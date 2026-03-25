package com.kin.easynotes.presentation.components

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2-HMAC-SHA256 hashing for passcode and pattern lock credentials.
 *
 * Stored format: "<base64(salt)>:<base64(hash)>"
 *
 * Using 120 000 iterations at 256 bits matches OWASP 2024 guidance for
 * PBKDF2-HMAC-SHA256 on interactive logins (short numeric/gesture inputs).
 */
object CredentialHasher {

    private const val ALGORITHM         = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS        = 120_000
    private const val KEY_LENGTH_BITS   = 256
    private const val SALT_LENGTH_BYTES = 16
    private val B64 = Base64.NO_WRAP

    /** Hash a raw credential string and return a storable "<salt>:<hash>" string. */
    fun hash(credential: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(credential, salt)
        return "${Base64.encodeToString(salt, B64)}:${Base64.encodeToString(hash, B64)}"
    }

    /**
     * Constant-time comparison of [candidate] against the stored [storedHash].
     * Returns false (safe) if the stored value is null or malformed.
     */
    fun verify(candidate: String, storedHash: String?): Boolean {
        if (storedHash == null) return false
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        return try {
            val salt         = Base64.decode(parts[0], B64)
            val expectedHash = Base64.decode(parts[1], B64)
            val candidateHash = pbkdf2(candidate, salt)
            constantTimeEquals(expectedHash, candidateHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec    = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash    = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return hash
    }

    /** Timing-safe byte array comparison (prevents timing oracle attacks). */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }
}
