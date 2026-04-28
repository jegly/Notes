package com.kin.easynotes.presentation.components

import com.kin.easynotes.security.Argon2Helper

/**
 * Password hashing and verification using Argon2id (via Argon2Helper).
 *
 * Replaces the previous PBKDF2-based implementation.
 * Only used for verifying the user's app password at login — note encryption
 * uses the Keystore-backed key in KeystoreManager, not this.
 */
object CredentialHasher {

    fun hash(credential: String): String = Argon2Helper.hash(credential)

    fun verify(candidate: String, storedHash: String?): Boolean =
        Argon2Helper.verify(candidate, storedHash)
}
