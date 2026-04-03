package com.kin.easynotes.security

import android.util.Base64
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2KtResult
import com.lambdapioneer.argon2kt.Argon2Mode
import java.security.SecureRandom

/**
 * Argon2id password hashing — memory-hard, GPU/ASIC resistant.
 *
 * Parameters (OWASP 2024 recommendation for interactive logins):
 *   m = 65536  (64 MB memory)
 *   t = 3      (3 iterations)
 *   p = 1      (1 thread)
 *   outputLen  = 32 bytes (256 bits)
 *
 * Stored hash format: "<base64-salt>$<argon2id-encoded-hash>"
 * The argon2kt library encodes parameters inside the hash string automatically.
 *
 * This replaces PBKDF2 in CredentialHasher for the app password.
 * It is NOT used for note encryption — that uses the Keystore key directly.
 */
object Argon2Helper {

    private const val SALT_BYTES   = 16
    private const val MEMORY_KB    = 65536   // 64 MB
    private const val ITERATIONS   = 3
    private const val PARALLELISM  = 1
    private const val OUTPUT_BYTES = 32

    private val argon2 = Argon2Kt()

    /**
     * Hash a password with a random salt.
     * Returns the encoded hash string (includes parameters + salt).
     * Safe to store in DataStore.
     */
    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val result = argon2.hash(
            mode       = Argon2Mode.ARGON2_ID,
            password   = password.toByteArray(Charsets.UTF_8),
            salt       = salt,
            tCostInIterations = ITERATIONS,
            mCostInKibibyte   = MEMORY_KB,
            parallelism       = PARALLELISM,
            hashLengthInBytes = OUTPUT_BYTES
        )
        return result.encodedOutputAsString()
    }

    /**
     * Constant-time verification of a password against a stored hash.
     */
    fun verify(password: String, storedHash: String?): Boolean {
        if (storedHash == null) return false
        return try {
            argon2.verify(
                mode         = Argon2Mode.ARGON2_ID,
                encoded      = storedHash,
                password     = password.toByteArray(Charsets.UTF_8)
            )
        } catch (e: Exception) {
            false
        }
    }
}
