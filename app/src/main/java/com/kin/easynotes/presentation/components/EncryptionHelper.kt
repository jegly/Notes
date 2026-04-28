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

    fun encrypt(data: String): String {
        if (mutableSessionToken.isEmpty()) throw IllegalStateException("Session not active")
        return keystoreManager.encrypt(data).getOrThrow()
    }

    fun decrypt(data: String): Pair<String?, DecryptionResult> {
        if (mutableSessionToken.isEmpty()) return Pair(null, DecryptionResult.LOADING)
        if (data.isBlank())               return Pair(null, DecryptionResult.BLANK_DATA)
        return try {
            val parts = data.split(":")
            when (parts.size) {
                2 -> {
                    val result = keystoreManager.decrypt(data)
                    if (result.isSuccess) Pair(result.getOrNull(), DecryptionResult.SUCCESS)
                    else Pair(null, DecryptionResult.BAD_PASSWORD)
                }
                3 -> Pair(null, DecryptionResult.LEGACY_FORMAT) // pre-v2 format
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
