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
import com.kin.easynotes.domain.model.Note
import com.kin.easynotes.domain.model.Settings
import com.kin.easynotes.domain.usecase.ImportExportUseCase
import com.kin.easynotes.domain.usecase.ImportResult
import com.kin.easynotes.domain.usecase.NoteUseCase
import com.kin.easynotes.domain.usecase.SettingsUseCase
import com.kin.easynotes.presentation.components.CredentialHasher
import com.kin.easynotes.presentation.components.EncryptionHelper
import com.kin.easynotes.presentation.components.GalleryObserver
import com.kin.easynotes.presentation.navigation.NavRoutes
import com.kin.easynotes.security.BackupCrypto
import com.kin.easynotes.security.KeystoreManager
import org.json.JSONArray
import org.json.JSONObject
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
        resetFailedLogins()
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
    // Brute-force protection
    // -------------------------------------------------------------------------

    /** Milliseconds until login attempts are allowed again. 0 = not locked out. */
    fun lockoutRemainingMs(): Long {
        val until = _settings.value.lockoutUntil.toLongOrNull() ?: 0L
        return (until - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    /**
     * Record a failed password attempt. After [FREE_ATTEMPTS] consecutive
     * failures, every further failure imposes a cooldown that doubles each
     * time (30s, 1m, 2m … capped at 30 minutes). Persisted, so killing the
     * app does not reset the counter.
     */
    fun registerFailedLogin() {
        val attempts = _settings.value.failedLoginAttempts + 1
        var lockoutUntil = _settings.value.lockoutUntil
        if (attempts >= FREE_ATTEMPTS) {
            val delayMs = (BASE_LOCKOUT_MS shl (attempts - FREE_ATTEMPTS).coerceAtMost(6))
                .coerceAtMost(MAX_LOCKOUT_MS)
            lockoutUntil = (System.currentTimeMillis() + delayMs).toString()
        }
        update(_settings.value.copy(failedLoginAttempts = attempts, lockoutUntil = lockoutUntil))
    }

    private fun resetFailedLogins() {
        if (_settings.value.failedLoginAttempts != 0 || _settings.value.lockoutUntil != "0") {
            update(_settings.value.copy(failedLoginAttempts = 0, lockoutUntil = "0"))
        }
    }

    private companion object {
        const val FREE_ATTEMPTS   = 5
        const val BASE_LOCKOUT_MS = 30_000L
        const val MAX_LOCKOUT_MS  = 30 * 60_000L
    }

    // -------------------------------------------------------------------------
    // First-time setup
    // -------------------------------------------------------------------------

    /**
     * First-time setup. [onComplete] receives false if setup did NOT succeed — the caller
     * must not navigate into the app in that case, or it lands on the note list with no
     * session and no key, where every save fails.
     */
    fun setPasswordFirstTime(password: String, context: Context, onComplete: (Boolean) -> Unit) {
        if (!keystoreManager.isDeviceSecure()) {
            showToast(context.getString(R.string.error_no_screen_lock), context)
            onComplete(false)
            return
        }
        isReEncrypting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            fun fail(message: String) {
                sessionPassword = null
                encryptionHelper.removePassword()
                viewModelScope.launch(Dispatchers.Main) {
                    isReEncrypting.value = false
                    showToast(message, context)
                    onComplete(false)
                }
            }

            // Generate Keystore key
            val keyResult = keystoreManager.generateKey()
            if (keyResult.isFailure) {
                val error = keyResult.exceptionOrNull()
                fail(
                    if (error is KeystoreManager.KeystoreError) error.message
                    else "Failed to generate secure key: ${error?.message}"
                )
                return@launch
            }

            // Activate session so EncryptionHelper can encrypt
            encryptionHelper.setPassword(password)
            sessionPassword = password

            // Re-encrypt any existing plain notes. If this fails the key is unusable, so
            // bail out rather than recording a password hash the notes aren't encrypted under.
            val reEncrypt = noteUseCase.reEncryptAllNotes(null)
            if (reEncrypt.isFailure) {
                fail("Could not encrypt existing notes: ${reEncrypt.exceptionOrNull()?.message}")
                return@launch
            }

            // Hash password with Argon2id and save
            val hash = CredentialHasher.hash(password)
            update(_settings.value.copy(passwordHash = hash))
            noteUseCase.observe()

            withContext(Dispatchers.Main) {
                isReEncrypting.value = false
                onComplete(true)
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
    /**
     * Returns the notes to export, or null if the in-memory set is not provably complete.
     *
     * noteUseCase.notes holds only successfully decrypted notes. Exporting it blind means a
     * locked session or a single undecryptable note yields a short — possibly empty — file
     * reported as a success, which is silent data loss at the exact moment the user is
     * trying to protect their notes.
     */
    private suspend fun completeNotesOrNull(): List<Note>? {
        val decrypted = noteUseCase.notes
        val rawCount = noteUseCase.rawNoteCount()
        return if (rawCount >= 0 && decrypted.size == rawCount) decrypted else null
    }

    private fun incompleteExportMessage(): String =
        "Export cancelled: not all notes could be read. Unlock the app and make sure every " +
        "note opens correctly, then try again."

    fun onExportAllAsTxt(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notes = completeNotesOrNull() ?: run {
                    withContext(Dispatchers.Main) { showToast(incompleteExportMessage(), context) }
                    return@launch
                }
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

    /**
     * Export all notes to a passphrase-protected file (Argon2id + AES-256-GCM).
     * Restorable on any device via [onRestoreEncryptedBackup] — this is the
     * supported migration path, since note encryption keys are device-bound.
     */
    fun onExportEncryptedBackup(uri: Uri, passphrase: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exportable = completeNotesOrNull() ?: run {
                    withContext(Dispatchers.Main) { showToast(incompleteExportMessage(), context) }
                    return@launch
                }
                val array = JSONArray()
                exportable.forEach { note ->
                    array.put(JSONObject().apply {
                        put("name", note.name)
                        put("description", note.description)
                        put("pinned", note.pinned)
                        put("createdAt", note.createdAt)
                    })
                }
                val blob = BackupCrypto.encrypt(array.toString().toByteArray(Charsets.UTF_8), passphrase)
                context.contentResolver.openOutputStream(uri)?.use { it.write(blob) }
                    ?: throw IOException("Could not open output file")
                withContext(Dispatchers.Main) {
                    showToast(context.getString(R.string.backup_created), context)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("${context.getString(R.string.backup_failed)}: ${e.message}", context)
                }
            }
        }
    }

    /** Restore notes from an encrypted backup file. Notes are re-encrypted with this device's Keystore key. */
    fun onRestoreEncryptedBackup(uri: Uri, passphrase: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val blob = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("Could not open backup file")
                val json = String(BackupCrypto.decrypt(blob, passphrase), Charsets.UTF_8)
                val array = JSONArray(json)
                var failed = 0
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val result = noteUseCase.addNote(
                        Note(
                            name        = obj.optString("name"),
                            description = obj.optString("description"),
                            pinned      = obj.optBoolean("pinned", false),
                            createdAt   = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                    if (result.isFailure) failed++
                }
                withContext(Dispatchers.Main) {
                    // Don't claim success when notes were silently dropped.
                    if (failed > 0) {
                        showToast("Restored ${array.length() - failed} of ${array.length()} notes", context)
                    } else {
                        showToast(context.getString(R.string.restore_success), context)
                    }
                }
            } catch (e: BackupCrypto.BadPassphraseOrCorrupt) {
                withContext(Dispatchers.Main) {
                    showToast(context.getString(R.string.restore_bad_passphrase), context)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("${context.getString(R.string.file_import_error)}: ${e.message}", context)
                }
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
        // withContext, not a nested runBlocking: this is already inside a coroutine, and
        // runBlocking here parked the main thread for the whole DataStore read.
        val loadedSettings = withContext(Dispatchers.IO) {
            settingsUseCase.loadSettingsFromRepository()
        }
        _settings.value = loadedSettings
        loadDefaultRoute()
    }

    init {
        runBlocking { loadSettings() }
    }
}
