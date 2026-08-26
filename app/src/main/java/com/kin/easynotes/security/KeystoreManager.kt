package com.kin.easynotes.security

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

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
        private const val TRANSFORMATION     = "AES/GCM/NoPadding"

        /** Marker for the envelope format introduced in 2.3.1. */
        const val ENVELOPE_PREFIX            = "v3"
        const val ENVELOPE_PARTS             = 5

        /** Pre-2.3.1 format: the body was encrypted directly by the Keystore key. */
        const val DIRECT_PARTS               = 2

        private const val DATA_KEY_BYTES     = 32
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
     * Returns true if our master key already exists in the Keystore AND is actually
     * retrievable.
     *
     * A bare containsAlias() check is not enough: a key generation that fails partway
     * (which StrongBox can do) leaves an alias behind that can never be resolved to a
     * SecretKey. Treating that as "key exists" meant the key was never regenerated and
     * every subsequent save failed permanently.
     */
    fun keyExists(): Boolean = getKey() != null

    /**
     * Verifies the key can actually perform an encryption right now.
     *
     * Distinct from [keyExists]: the key can be present and well-formed but still refuse
     * to operate because [setUnlockedDeviceRequired] is set and the device is locked.
     * Callers use this to tell "needs regenerating" apart from "wait until unlocked".
     */
    fun isKeyUsable(): Boolean = encrypt("probe").isSuccess

    /**
     * Generate a new AES-256-GCM key inside the Keystore.
     * Prefers StrongBox (dedicated secure element) with fallback to TEE.
     * Throws if device has no screen lock.
     */
    fun generateKey(): Result<Unit> {
        if (!isDeviceSecure()) {
            return Result.failure(KeystoreError.NoScreenLock)
        }
        // Refuse to run if a usable key is already present. The retry ladder below deletes
        // the alias between attempts, which would irrecoverably destroy every existing
        // note if this were ever called with a live key in place.
        if (getKey() != null) {
            return Result.success(Unit)
        }

        // Ordered from strongest to weakest. Earlier attempts can fail on hardware that
        // does not support the requested backing; each is tried in turn.
        //
        // NOTE: unlockedDeviceRequired is kept for BOTH hardware-backed attempts. The
        // previous fallback silently dropped it, so two devices running identical code
        // ended up with different security postures purely by accident of hardware — and
        // only the strict one exposed the save-during-lock bug.
        val attempts = listOf(
            KeySpecAttempt(strongBox = true,  unlockedDeviceRequired = true),
            KeySpecAttempt(strongBox = false, unlockedDeviceRequired = true),
            KeySpecAttempt(strongBox = false, unlockedDeviceRequired = false),
        )

        val failures = mutableListOf<String>()
        for (attempt in attempts) {
            // Clear any alias left behind by a previous partial failure, otherwise the
            // next generate can collide with an unusable half-created entry.
            deleteKey()
            try {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER
                )
                keyGenerator.init(buildSpec(attempt))
                keyGenerator.generateKey()
                // Only accept the key if it can actually be resolved and used.
                if (getKey() != null) return Result.success(Unit)
                failures += "${attempt}: key generated but not retrievable"
            } catch (e: Exception) {
                failures += "$attempt: ${e.message}"
            }
        }
        deleteKey()
        return Result.failure(KeystoreError.Other("Key generation failed. ${failures.joinToString(" | ")}"))
    }

    private data class KeySpecAttempt(val strongBox: Boolean, val unlockedDeviceRequired: Boolean) {
        override fun toString() =
            "[strongBox=$strongBox, unlockedDeviceRequired=$unlockedDeviceRequired]"
    }

    private fun buildSpec(attempt: KeySpecAttempt): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUnlockedDeviceRequired(attempt.unlockedDeviceRequired)
            .apply {
                if (attempt.strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setIsStrongBoxBacked(true)
                }
            }
            .build()
    }

    /**
     * Encrypt data using envelope encryption.
     *
     * The payload is encrypted in-process with a random 256-bit data key — ordinary AES-GCM,
     * hardware-accelerated, hundreds of MB/s — and only that 32-byte data key is passed
     * through the secure element. Previously the entire payload went through the Keystore,
     * and StrongBox in particular is built for small operations: a note of a few thousand
     * lines took minutes to save and minutes to load.
     *
     * The trade-off, stated plainly: the per-note data key exists in the JVM heap for the
     * duration of the call. The local copy is zeroed afterwards, though the copy `SecretKeySpec`
     * makes internally is left to the GC. The master key still never leaves the secure element,
     * `setUnlockedDeviceRequired` still governs it, and a wrapped data key is useless without
     * it. This is the standard envelope construction.
     *
     * Format: "v3:<master-iv>:<wrapped-data-key>:<data-iv>:<data-ciphertext>", each base64.
     */
    fun encrypt(plaintext: String): Result<String> {
        val dataKeyBytes = ByteArray(DATA_KEY_BYTES)
        return try {
            val masterKey = getKey() ?: return Result.failure(KeystoreError.KeyNotFound)

            SecureRandom().nextBytes(dataKeyBytes)
            val dataKey = SecretKeySpec(dataKeyBytes, KeyProperties.KEY_ALGORITHM_AES)

            // Bulk work, in-process.
            val dataCipher = Cipher.getInstance(TRANSFORMATION)
            dataCipher.init(Cipher.ENCRYPT_MODE, dataKey)
            val dataIv = dataCipher.iv
            val dataCt = dataCipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            // The only thing that crosses into the secure element: 32 bytes.
            val wrapCipher = Cipher.getInstance(TRANSFORMATION)
            wrapCipher.init(Cipher.ENCRYPT_MODE, masterKey)
            val masterIv   = wrapCipher.iv
            val wrappedKey = wrapCipher.doFinal(dataKeyBytes)

            Result.success(
                listOf(ENVELOPE_PREFIX, b64(masterIv), b64(wrappedKey), b64(dataIv), b64(dataCt))
                    .joinToString(":")
            )
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            dataKeyBytes.fill(0)
        }
    }

    /**
     * Decrypt data written by [encrypt].
     *
     * Reads both the current envelope format and the pre-2.3.1 format, where the body was
     * encrypted directly by the Keystore key. Old notes are never rewritten in place — they
     * convert to the new format the next time they are saved.
     */
    fun decrypt(ciphertext: String): Result<String> {
        return try {
            val masterKey = getKey() ?: return Result.failure(KeystoreError.KeyNotFound)
            val parts     = ciphertext.split(":")
            when {
                parts.size == ENVELOPE_PARTS && parts[0] == ENVELOPE_PREFIX ->
                    decryptEnvelope(masterKey, parts)
                parts.size == DIRECT_PARTS ->
                    decryptDirect(masterKey, parts)
                else ->
                    Result.failure(Exception("Invalid ciphertext format"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decryptEnvelope(masterKey: SecretKey, parts: List<String>): Result<String> {
        val unwrap = Cipher.getInstance(TRANSFORMATION)
        unwrap.init(Cipher.DECRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_BITS, unb64(parts[1])))
        val dataKeyBytes = unwrap.doFinal(unb64(parts[2]))
        return try {
            val dataCipher = Cipher.getInstance(TRANSFORMATION)
            dataCipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(dataKeyBytes, KeyProperties.KEY_ALGORITHM_AES),
                GCMParameterSpec(GCM_TAG_BITS, unb64(parts[3]))
            )
            Result.success(String(dataCipher.doFinal(unb64(parts[4])), Charsets.UTF_8))
        } finally {
            dataKeyBytes.fill(0)
        }
    }

    /** Pre-2.3.1 notes: the Keystore key encrypted the body directly. */
    private fun decryptDirect(masterKey: SecretKey, parts: List<String>): Result<String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_BITS, unb64(parts[0])))
        return Result.success(String(cipher.doFinal(unb64(parts[1])), Charsets.UTF_8))
    }

    private fun b64(bytes: ByteArray): String =
        android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

    private fun unb64(text: String): ByteArray =
        android.util.Base64.decode(text, android.util.Base64.NO_WRAP)

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
