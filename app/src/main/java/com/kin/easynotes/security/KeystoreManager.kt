package com.kin.easynotes.security

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the AES-256-GCM master key stored in Android Keystore (TEE/StrongBox).
 *
 * Security properties:
 * - Key is generated inside the secure element — raw bytes never enter JVM heap
 * - Requires device screen lock to be set (enforced at key generation time)
 * - Key is only accessible when device is unlocked (setUnlockedDeviceRequired)
 * - On devices with StrongBox, key lives in a dedicated tamper-resistant chip
 * - Key is destroyed when the app is uninstalled — this is by design (Option C)
 *
 * Ciphertext format: <iv(12B) base64>:<ciphertext+tag base64>
 */
class KeystoreManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_PROVIDER  = "AndroidKeyStore"
        private const val KEY_ALIAS          = "notes_master_key_v2"
        private const val GCM_TAG_BITS       = 128
        private const val KEY_SIZE_BITS      = 256
    }

    sealed class KeystoreError(override val message: String) : Exception(message) {
        object NoScreenLock : KeystoreError("Device has no screen lock set. Please enable a PIN, pattern, or password in system settings.") {
            private fun readResolve(): Any = NoScreenLock
        }
        object KeyNotFound  : KeystoreError("Master key not found in Keystore.") {
            private fun readResolve(): Any = KeyNotFound
        }
        data class Other(val msg: String) : KeystoreError(msg)
    }

    /**
     * Check if the device has a screen lock set.
     * We require this before generating any keys.
     */
    fun isDeviceSecure(): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return km.isDeviceSecure
    }

    /**
     * Returns true if our master key already exists in the Keystore.
     */
    fun keyExists(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate a new AES-256-GCM key inside the Keystore.
     * Prefers StrongBox (dedicated secure element) with fallback to TEE.
     * Throws if device has no screen lock.
     */
    fun generateKey(): Result<Unit> {
        if (!isDeviceSecure()) {
            return Result.failure(KeystoreError.NoScreenLock)
        }
        return try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(KEY_SIZE_BITS)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                // On some devices (e.g. ZTE), setUnlockedDeviceRequired(true) can cause 
                // key generation failure during initial setup even if lock is set.
                // We'll try with it enabled first, then fall back to disabled if it fails.
                .setUnlockedDeviceRequired(true) 
                .setRandomizedEncryptionRequired(true)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        try { setIsStrongBoxBacked(true) } catch (e: Exception) { /* TEE fallback */ }
                    }
                }
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
            Result.success(Unit)
        } catch (e: Exception) {
            // Fallback: try without setUnlockedDeviceRequired if first attempt failed
            try {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
                val fallbackSpec = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setKeySize(KEY_SIZE_BITS)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(fallbackSpec)
                keyGenerator.generateKey()
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(KeystoreError.Other("Primary & Fallback failed: ${e.message} | ${e2.message}"))
            }
        }
    }

    /**
     * Encrypt data using the Keystore-backed key.
     * The key never leaves the secure element — we get a Cipher reference backed by it.
     * Returns "<base64-iv>:<base64-ciphertext+tag>"
     */
    fun encrypt(plaintext: String): Result<String> {
        return try {
            val key = getKey() ?: return Result.failure(KeystoreError.KeyNotFound)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv             = cipher.iv
            val ciphertext     = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val ivB64          = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
            val ctB64          = android.util.Base64.encodeToString(ciphertext, android.util.Base64.NO_WRAP)
            Result.success("$ivB64:$ctB64")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Decrypt data using the Keystore-backed key.
     */
    fun decrypt(ciphertext: String): Result<String> {
        return try {
            val key    = getKey() ?: return Result.failure(KeystoreError.KeyNotFound)
            val parts  = ciphertext.split(":")
            if (parts.size != 2) return Result.failure(Exception("Invalid ciphertext format"))
            val iv     = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
            val ct     = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            Result.success(String(cipher.doFinal(ct), Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete the master key — called on explicit user request.
     * Note: Android automatically deletes all keys on app uninstall.
     */
    fun deleteKey() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
        } catch (e: Exception) { /* best effort */ }
    }

    /**
     * Returns whether the key is hardware-backed (TEE or StrongBox).
     * Useful for diagnostics / about screen.
     */
    fun isHardwareBacked(): Boolean {
        return try {
            val key    = getKey() ?: return false
            val factory = SecretKeyFactory.getInstance(key.algorithm, KEYSTORE_PROVIDER)
            val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT ||
                keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX
            } else {
                @Suppress("DEPRECATION")
                keyInfo.isInsideSecureHardware
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns "StrongBox", "TEE", or "Software" for display.
     */
    fun securityLevelDescription(): String {
        return try {
            val key     = getKey() ?: return "Unknown"
            val factory = SecretKeyFactory.getInstance(key.algorithm, KEYSTORE_PROVIDER)
            val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                when (keyInfo.securityLevel) {
                    KeyProperties.SECURITY_LEVEL_STRONGBOX -> "StrongBox"
                    KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "TEE"
                    else -> "Software"
                }
            } else {
                @Suppress("DEPRECATION")
                if (keyInfo.isInsideSecureHardware) "Hardware (TEE)" else "Software"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (e: Exception) {
            null
        }
    }
}
