package com.kin.easynotes.presentation.screens.settings.model

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kin.easynotes.BuildConfig
import com.kin.easynotes.R
import com.kin.easynotes.data.repository.ImportExportRepository
import com.kin.easynotes.data.repository.BackupResult
import com.kin.easynotes.domain.model.Settings
import com.kin.easynotes.domain.usecase.ImportExportUseCase
import com.kin.easynotes.domain.usecase.ImportResult
import com.kin.easynotes.domain.usecase.NoteUseCase
import com.kin.easynotes.domain.usecase.SettingsUseCase
import com.kin.easynotes.presentation.components.CredentialHasher
import com.kin.easynotes.presentation.components.EncryptionHelper
import com.kin.easynotes.presentation.components.GalleryObserver
import com.kin.easynotes.presentation.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val galleryObserver: GalleryObserver,
    val backup: ImportExportRepository,
    private val settingsUseCase: SettingsUseCase,
    val noteUseCase: NoteUseCase,
    private val importExportUseCase: ImportExportUseCase,
    private val encryptionHelper: EncryptionHelper,
) : ViewModel() {

    var defaultRoute: String? = null
    var pendingWidgetNoteId: Int = -1
    val databaseUpdate = mutableStateOf(false)

    // The plaintext password held in memory for the session.
    // Used for backup export/import and loaded into EncryptionHelper.
    var sessionPassword: String? = null
        private set

    private val _settings = mutableStateOf(Settings())
    var settings: State<Settings> = _settings

    // Whether a re-encryption operation is in progress
    val isReEncrypting = mutableStateOf(false)

    fun loadDefaultRoute() {
        defaultRoute = if (!_settings.value.isSetup) {
            NavRoutes.PasswordSetup.route
        } else {
            NavRoutes.Login.route
        }
    }

    /** Called after successful login — loads password into EncryptionHelper and starts observing notes */
    fun onLoginSuccess(password: String) {
        sessionPassword = password
        encryptionHelper.setPassword(password)
        noteUseCase.observe()
        defaultRoute = NavRoutes.Home.route
    }

    /** Set password for the first time — hashes it, re-encrypts all notes, saves */
    fun setPasswordFirstTime(password: String, context: Context, onComplete: () -> Unit) {
        isReEncrypting.value = true
        sessionPassword = password
        encryptionHelper.setPassword(password)
        viewModelScope.launch(Dispatchers.IO) {
            noteUseCase.reEncryptAllNotes(null)
            val newHash = CredentialHasher.hash(password)
            update(_settings.value.copy(passwordHash = newHash))
            withContext(Dispatchers.Main) {
                isReEncrypting.value = false
                onComplete()
            }
        }
    }

    /** Change password — decrypts all notes with old, re-encrypts with new */
    fun changePassword(oldPassword: String, newPassword: String, context: Context, onComplete: (Boolean) -> Unit) {
        if (!CredentialHasher.verify(oldPassword, _settings.value.passwordHash)) {
            onComplete(false)
            return
        }
        isReEncrypting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            encryptionHelper.setPassword(newPassword)
            sessionPassword = newPassword
            noteUseCase.reEncryptAllNotes(oldPassword)
            val newHash = CredentialHasher.hash(newPassword)
            update(_settings.value.copy(passwordHash = newHash))
            withContext(Dispatchers.Main) {
                isReEncrypting.value = false
                onComplete(true)
            }
        }
    }

    /** Verify password — used at login */
    fun verifyPassword(password: String): Boolean =
        CredentialHasher.verify(password, _settings.value.passwordHash)

    fun update(newSettings: Settings) {
        _settings.value = newSettings.copy()
        viewModelScope.launch {
            settingsUseCase.saveSettingsToRepository(newSettings)
        }
    }

    fun onExportBackup(uri: Uri, context: Context) {
        viewModelScope.launch {
            // Always encrypt using session password
            val result = backup.exportBackup(uri, sessionPassword)
            handleBackupResult(result, context)
            databaseUpdate.value = true
        }
    }

    fun onImportBackup(uri: Uri, context: Context) {
        viewModelScope.launch {
            val result = backup.importBackup(uri, sessionPassword)
            handleBackupResult(result, context)
            if (result is BackupResult.Success) noteUseCase.observe()
            databaseUpdate.value = true
        }
    }

    /**
     * Restore with an explicit password — used when restoring a backup that
     * may have been created on a different device with a different password.
     * After successful restore the notes in the DB are encrypted with the
     * backup password; we re-encrypt them with the current session password.
     */
    fun onImportBackupWithPassword(uri: Uri, backupPassword: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = backup.importBackup(uri, backupPassword)
            withContext(Dispatchers.Main) {
                handleBackupResult(result, context)
            }
            if (result is BackupResult.Success) {
                // Re-encrypt all restored notes from backupPassword → current sessionPassword
                if (backupPassword != sessionPassword) {
                    val tempHelper = com.kin.easynotes.presentation.components.EncryptionHelper(
                        StringBuilder(backupPassword)
                    )
                    // Temporarily swap helper so reEncryptAllNotes can read with old password
                    encryptionHelper.setPassword(backupPassword)
                    noteUseCase.reEncryptAllNotes(null) // decrypt with backupPassword, re-encrypt with same
                    // Now switch to current session password and re-encrypt
                    encryptionHelper.setPassword(sessionPassword ?: backupPassword)
                    noteUseCase.reEncryptAllNotes(backupPassword)
                }
                withContext(Dispatchers.Main) {
                    noteUseCase.observe()
                    databaseUpdate.value = true
                }
            }
        }
    }

    fun onImportFiles(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            importExportUseCase.importNotes(uris) { result ->
                handleImportResult(result, context)
            }
        }
    }

    private fun getLocaleListFromXml(context: Context): LocaleListCompat {
        val tagsList = mutableListOf<CharSequence>()
        try {
            val xpp: XmlPullParser = context.resources.getXml(com.kin.easynotes.R.xml.locales_config)
            while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.eventType == XmlPullParser.START_TAG) {
                    if (xpp.name == "locale") tagsList.add(xpp.getAttributeValue(0))
                }
                xpp.next()
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
    }

    fun getSupportedLanguages(context: Context): Map<String, String> {
        val localeList = getLocaleListFromXml(context)
        val map = mutableMapOf<String, String>()
        for (a in 0 until localeList.size()) {
            localeList[a]?.let { map[it.getDisplayName(it)] = it.toLanguageTag() }
        }
        return map
    }

    private fun handleBackupResult(result: BackupResult, context: Context) {
        when (result) {
            is BackupResult.Success -> {}
            is BackupResult.Error -> showToast("Error: ${result.message}", context)
            BackupResult.BadPassword -> showToast(context.getString(R.string.detabase_restore_error), context)
        }
    }

    private fun handleImportResult(result: ImportResult, context: Context) {
        when (result.successful) {
            result.total -> showToast(context.getString(R.string.file_import_success), context)
            0 -> showToast(context.getString(R.string.file_import_error), context)
            else -> showToast(context.getString(R.string.file_import_partial_error), context)
        }
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val version: String = BuildConfig.VERSION_NAME
    val build: String = BuildConfig.BUILD_TYPE

    private suspend fun loadSettings() {
        val loadedSettings = runBlocking(Dispatchers.IO) {
            settingsUseCase.loadSettingsFromRepository()
        }
        _settings.value = loadedSettings
        defaultRoute = if (!loadedSettings.isSetup) {
            NavRoutes.PasswordSetup.route
        } else {
            NavRoutes.Login.route
        }
    }

    init {
        runBlocking { loadSettings() }
    }
}
