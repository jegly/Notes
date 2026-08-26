package com.kin.easynotes.presentation.components

import com.kin.easynotes.security.KeystoreManager

/**
 * Thin wrapper around KeystoreManager that preserves the existing interface
 * used throughout the app (encrypt/decrypt/isPasswordEmpty/setPassword/removePassword).
 *
 * V2 changes:
 * - All crypto operations now go through KeystoreManager → Android Keystore (TEE/StrongBox)
 * - Raw key bytes never enter JVM heap
 * - "Password" here is only used for login verification (via Argon2id in CredentialHasher)
 *   and is tracked only to know if the session is active
 * - setPassword() is called at login success — it signals that the Keystore key
 *   is now accessible (device is unlocked) and starts the session
 */
class EncryptionHelper(
    private val mutableSessionToken: StringBuilder,
    val keystoreManager: KeystoreManager
) {
    fun isPasswordEmpty(): Boolean = mutableSessionToken.isEmpty()

    fun removePassword() {
        mutableSessionToken.setLength(0)
        // Zeroize the buffer explicitly
        for (i in 0 until mutableSessionToken.capacity()) {
            try { mutableSessionToken.setCharAt(i, '\u0000') } catch (e: Exception) { break }
        }
        mutableSessionToken.setLength(0)
    }

    fun setPassword(newPassword: String) {
        mutableSessionToken.setLength(0)
        mutableSessionToken.append(newPassword)
    }

    /**
     * Encrypt [data] with the Keystore-backed master key.
     *
     * Never throws. The master key is generated with `setUnlockedDeviceRequired(true)`,
     * so the secure element refuses to use it while the device is locked — an encrypt
     * attempted on a lock transition (screen off, fold closed, power button) legitimately
     * fails. Callers must treat failure as "could not save right now" and retain the
     * plaintext, rather than letting it escape as an uncaught exception.
     */
    fun encrypt(data: String): Result<String> {
        if (mutableSessionToken.isEmpty()) {
            return Result.failure(SessionNotActive)
        }
        return keystoreManager.encrypt(data)
    }

    /** Thrown-free marker for "the app is locked, there is no session to encrypt under". */
    object SessionNotActive : IllegalStateException("Session not active") {
        private fun readResolve(): Any = SessionNotActive
    }

    fun decrypt(data: String): Pair<String?, DecryptionResult> {
        if (mutableSessionToken.isEmpty()) return Pair(null, DecryptionResult.LOADING)
        if (data.isBlank())               return Pair(null, DecryptionResult.BLANK_DATA)
        return try {
            val parts = data.split(":")
            val isEnvelope = parts.size == KeystoreManager.ENVELOPE_PARTS &&
                    parts[0] == KeystoreManager.ENVELOPE_PREFIX
            when {
                // v3 envelope (2.3.1+) and the older direct-Keystore form are both read by
                // KeystoreManager; it picks the right path off the same string.
                isEnvelope || parts.size == KeystoreManager.DIRECT_PARTS -> {
                    val result = keystoreManager.decrypt(data)
                    if (result.isSuccess) Pair(result.getOrNull(), DecryptionResult.SUCCESS)
                    else Pair(null, DecryptionResult.BAD_PASSWORD)
                }
                parts.size == 3 -> Pair(null, DecryptionResult.LEGACY_FORMAT) // pre-v2 format
                else -> Pair(null, DecryptionResult.INVALID_DATA)
            }
        } catch (e: Exception) {
            Pair(null, DecryptionResult.BAD_PASSWORD)
        }
    }
}

enum class DecryptionResult {
    EMPTY,
    SUCCESS,
    INVALID_DATA,
    BLANK_DATA,
    BAD_PASSWORD,
    LOADING,
    LEGACY_FORMAT
}
