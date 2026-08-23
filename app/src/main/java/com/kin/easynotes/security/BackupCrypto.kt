package com.kin.easynotes.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Device-independent encrypted backup format.
 *
 * Unlike note storage (Keystore-bound AES key that never leaves this device),
 * backups are protected by a key derived from a user passphrase with Argon2id,
 * so they can be restored on any device — including after uninstall.
 *
 * File layout (binary):
 *   magic   8 bytes  "NOTESBK2"
 *   version 1 byte   0x01
 *   salt    16 bytes Argon2id salt
 *   iv      12 bytes AES-GCM nonce
 *   body    N bytes  AES-256-GCM ciphertext + 16-byte tag
 */
object BackupCrypto {

    class BadPassphraseOrCorrupt : Exception("Wrong passphrase or corrupted backup file")
    class NotABackupFile : Exception("Not a Notes backup file")

    private val MAGIC = "NOTESBK2".toByteArray(Charsets.US_ASCII)
    private const val VERSION: Byte = 1
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val GCM_TAG_BITS = 128

    fun encrypt(plaintext: ByteArray, passphrase: String): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also { random.nextBytes(it) }
        val iv   = ByteArray(IV_BYTES).also { random.nextBytes(it) }
        val key  = Argon2Helper.deriveKey(passphrase, salt)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
            val body = cipher.doFinal(plaintext)
            return MAGIC + byteArrayOf(VERSION) + salt + iv + body
        } finally {
            key.fill(0)
        }
    }

    fun decrypt(blob: ByteArray, passphrase: String): ByteArray {
        val headerLen = MAGIC.size + 1 + SALT_BYTES + IV_BYTES
        if (blob.size <= headerLen) throw NotABackupFile()
        if (!blob.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) throw NotABackupFile()
        if (blob[MAGIC.size] != VERSION) throw NotABackupFile()

        var offset = MAGIC.size + 1
        val salt = blob.copyOfRange(offset, offset + SALT_BYTES); offset += SALT_BYTES
        val iv   = blob.copyOfRange(offset, offset + IV_BYTES);   offset += IV_BYTES
        val body = blob.copyOfRange(offset, blob.size)

        val key = Argon2Helper.deriveKey(passphrase, salt)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
            return cipher.doFinal(body)
        } catch (e: Exception) {
            // GCM tag mismatch — wrong passphrase or tampered/corrupted file
            throw BadPassphraseOrCorrupt()
        } finally {
            key.fill(0)
        }
    }
}
