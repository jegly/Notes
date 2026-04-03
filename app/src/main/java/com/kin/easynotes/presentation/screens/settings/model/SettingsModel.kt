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
import com.kin.easynotes.domain.model.Settings
import com.kin.easynotes.domain.usecase.ImportExportUseCase
import com.kin.easynotes.domain.usecase.ImportResult
import com.kin.easynotes.domain.usecase.NoteUseCase
import com.kin.easynotes.domain.usecase.SettingsUseCase
import com.kin.easynotes.presentation.components.CredentialHasher
import com.kin.easynotes.presentation.components.EncryptionHelper
import com.kin.easynotes.presentation.components.GalleryObserver
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.security.KeystoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val settingsUseCase: SettingsUseCase,
    val noteUseCase: NoteUseCase,
    private val importExportUseCase: ImportExportUseCase,
    private val encryptionHelper: EncryptionHelper,
    val keystoreManager: KeystoreManager,
) : ViewModel() {

    var defaultRoute: String? = null
    var pendingWidgetNoteId: Int = -1
    val databaseUpdate = mutableStateOf(false)
    val isReEncrypting = mutableStateOf(false)

    // Plaintext password held in memory for session — used for Argon2id verification only
    var sessionPassword: String? = null
        private set

    private val _settings = mutableStateOf(Settings())
    var settings: State<Settings> = _settings

    // Auto-lock timer job
    private var autoLockJob: Job? = null

    // -------------------------------------------------------------------------
    // Routing
    // -------------------------------------------------------------------------

    fun loadDefaultRoute() {
        defaultRoute = when {
            !_settings.value.isSetup        -> NavRoutes.PasswordSetup.route
            sessionPassword == null         -> NavRoutes.Login.route
            else                            -> NavRoutes.Home.route
        }
    }

    // -------------------------------------------------------------------------
    // Login / session
    // -------------------------------------------------------------------------

    /** Called after successful password verification at LoginScreen */
    fun onLoginSuccess(password: String) {
        sessionPassword = password
        encryptionHelper.setPassword(password)
        noteUseCase.observe()
        cancelAutoLock()
        defaultRoute = NavRoutes.Home.route
    }

    /** Called when app goes to background — starts auto-lock countdown */
    fun onAppBackground() {
        val minutes = _settings.value.autoLockMinutes
        if (minutes == 0) return  // never auto-lock
        autoLockJob?.cancel()
        autoLockJob = viewModelScope.launch {
            delay(minutes * 60_000L)
            lock()
        }
    }

    /** Called when app returns to foreground — cancels countdown if still in time */
    fun onAppForeground() {
        cancelAutoLock()
    }

    fun cancelAutoLock() {
        autoLockJob?.cancel()
        autoLockJob = null
    }

    /** Lock the app — zeroize session, clear decrypted content */
    fun lock() {
        sessionPassword = null
        encryptionHelper.removePassword()
        noteUseCase.zeroize()
        defaultRoute = NavRoutes.Login.route
    }

    fun verifyPassword(password: String): Boolean =
        CredentialHasher.verify(password, _settings.value.passwordHash)

    // -------------------------------------------------------------------------
    // First-time setup
    // -------------------------------------------------------------------------

    fun setPasswordFirstTime(password: String, context: Context, onComplete: () -> Unit) {
        if (!keystoreManager.isDeviceSecure()) {
            showToast(context.getString(R.string.error_no_screen_lock), context)
            return
        }
        isReEncrypting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            // Generate Keystore key
            val keyResult = if (!keystoreManager.keyExists()) {
                keystoreManager.generateKey()
            } else Result.success(Unit)

            if (keyResult.isFailure) {
                withContext(Dispatchers.Main) {
                    isReEncrypting.value = false
                    val error = keyResult.exceptionOrNull()
                    val message = if (error is KeystoreManager.KeystoreError) error.message 
                                 else "Failed to generate secure key: ${error?.message}"
                    showToast(message, context)
                }
                return@launch
            }

            // Activate session so EncryptionHelper can encrypt
            encryptionHelper.setPassword(password)
            sessionPassword = password

            // Re-encrypt any existing plain notes
            noteUseCase.reEncryptAllNotes(null)

            // Hash password with Argon2id and save
            val hash = CredentialHasher.hash(password)
            update(_settings.value.copy(passwordHash = hash))

            withContext(Dispatchers.Main) {
                isReEncrypting.value = false
                onComplete()
            }
        }
    }

    // -------------------------------------------------------------------------
    // Change password
    // -------------------------------------------------------------------------

    fun changePassword(oldPassword: String, newPassword: String, context: Context, onComplete: (Boolean) -> Unit) {
        if (!CredentialHasher.verify(oldPassword, _settings.value.passwordHash)) {
            onComplete(false)
            return
        }
        if (!keystoreManager.isDeviceSecure()) {
            showToast(context.getString(R.string.error_no_screen_lock), context)
            onComplete(false)
            return
        }
        isReEncrypting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            // For Keystore approach, changing password only updates the Argon2id hash
            // The Keystore key itself doesn't change — it's device-bound hardware
            val newHash = CredentialHasher.hash(newPassword)
            sessionPassword = newPassword
            encryptionHelper.setPassword(newPassword)
            update(_settings.value.copy(passwordHash = newHash))

            withContext(Dispatchers.Main) {
                isReEncrypting.value = false
                onComplete(true)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Backup / restore
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Export / Import (txt only — backup removed, Keystore keys are device-bound)
    // -------------------------------------------------------------------------

    /**
     * Export all notes as a single plaintext txt file.
     * WARNING: This exports decrypted plaintext — user must acknowledge before calling.
     */
    fun onExportAllAsTxt(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notes = noteUseCase.notes
                val sb = StringBuilder()
                notes.forEachIndexed { index, note ->
                    sb.appendLine("=== Note ${index + 1} ===")
                    if (note.name.isNotBlank()) sb.appendLine("Title: ${note.name}")
                    sb.appendLine(note.description)
                    sb.appendLine()
                }
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(sb.toString().toByteArray(Charsets.UTF_8))
                }
                withContext(Dispatchers.Main) {
                    showToast(context.getString(R.string.file_import_success), context)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Export failed: ${e.message}", context)
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

    // -------------------------------------------------------------------------
    // Settings persistence
    // -------------------------------------------------------------------------

    fun update(newSettings: Settings) {
        _settings.value = newSettings.copy()
        viewModelScope.launch {
            settingsUseCase.saveSettingsToRepository(newSettings)
        }
    }

    // -------------------------------------------------------------------------
    // Language helpers
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Result handlers
    // -------------------------------------------------------------------------

    private fun handleImportResult(result: ImportResult, context: Context) {
        when (result.successful) {
            result.total -> showToast(context.getString(R.string.file_import_success), context)
            0            -> showToast(context.getString(R.string.file_import_error), context)
            else         -> showToast(context.getString(R.string.file_import_partial_error), context)
        }
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val version: String = BuildConfig.VERSION_NAME
    val build: String   = BuildConfig.BUILD_TYPE

    private suspend fun loadSettings() {
        val loadedSettings = runBlocking(Dispatchers.IO) {
            settingsUseCase.loadSettingsFromRepository()
        }
        _settings.value = loadedSettings
        loadDefaultRoute()
    }

    init {
        runBlocking { loadSettings() }
    }
}
