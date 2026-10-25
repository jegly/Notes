package com.kin.easynotes.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

/**
 * Refuses to run if the installed APK's current signer is anything other than the
 * pinned release certificate. Fails closed: any error returns false.
 *
 * Confirm the digests with: apksigner verify --print-certs <release.apk>
 */
class SignatureVerifier(private val context: Context) {

    fun isSignedByTrustedCert(): Boolean {
        return try {
            val signatures = getCurrentSignatures() ?: return false
            val md = MessageDigest.getInstance("SHA-256")
            val want = expected()
            signatures.any { md.digest(it.toByteArray()).contentEquals(want) }
        } catch (_: Throwable) {
            false
        }
    }

    private fun getCurrentSignatures(): Array<Signature>? {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNING_CERTIFICATES,
        )
        val signingInfo = info.signingInfo ?: return null
        // Current signer only — never signingCertificateHistory, which still contains key0.
        return signingInfo.apkContentsSigners
    }

    /** Reconstruct the expected 32-byte cert digest from the masked form (never materialized as hex). */
    private fun expected(): ByteArray {
        val masked = if (Build.VERSION.SDK_INT >= 37) P else M
        return ByteArray(masked.size) { (masked[it] xor K[it % K.size]).toByte() }
    }

    private companion object {
        // Expected signing-cert SHA-256 digests, stored XOR-masked (never as greppable hex —
        // string literals survive R8) and compared as raw bytes. M = jegly key0 (98d324d4…),
        // P = ML-DSA-87 PQC key (5f99de5b…).
        // API 37+ verifies the APK Signature Scheme v3.2 hybrid block, so the current signer is
        // the PQC key there. key0 is deliberately NOT accepted on API 37+: an APK re-signed with
        // only key0 would otherwise pass. Older releases ignore v3.2 and still see key0.
        val K = intArrayOf(0xDB, 0xFD, 0x08, 0x18, 0xE7, 0x26, 0x0F, 0xF0, 0x60)
        val M = intArrayOf(
            0x43, 0x2E, 0x2C, 0xCC, 0xF7, 0x4C, 0x39, 0x7C, 0x02, 0xA9, 0x67, 0x02, 0x3C, 0x3E, 0x8A, 0x95,
            0x9B, 0x27, 0x23, 0x51, 0x44, 0x7D, 0x62, 0x12, 0x8A, 0xC1, 0x90, 0x35, 0xB3, 0xBE, 0xB8, 0xAB,
        )
        val P = intArrayOf(
            0x84, 0x64, 0xD6, 0x43, 0xD6, 0xB3, 0x7A, 0xCD, 0x2F, 0x33, 0x20, 0xA3, 0xF3, 0x35, 0xDD, 0x28,
            0xC5, 0x0B, 0xFA, 0xF4, 0xDE, 0xE3, 0x55, 0x61, 0x10, 0x3B, 0xDD, 0x29, 0xE7, 0x96, 0x6E, 0x41,
        )
    }
}
