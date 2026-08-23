package com.kin.easynotes.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import java.security.MessageDigest

/**
 * Refuses to run if the installed APK is signed with anything other than the
 * pinned release certificate. Fails closed: any error returns false.
 *
 * Obtain a new digest with: apksigner verify --print-certs <release.apk>
 */
class SignatureVerifier(private val context: Context) {

    fun isSignedByTrustedCert(): Boolean {
        return try {
            val signatures = getCurrentSignatures() ?: return false
            signatures.any { sha256Hex(it.toByteArray()) == TRUSTED_CERT_SHA256 }
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
        return signingInfo.apkContentsSigners
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        val sb = StringBuilder(digest.size * 2)
        for (b in digest) {
            sb.append(HEX[(b.toInt() ushr 4) and 0x0F])
            sb.append(HEX[b.toInt() and 0x0F])
        }
        return sb.toString()
    }

    private companion object {
        const val TRUSTED_CERT_SHA256 =
            "98d324d4106a368c62729a0a24d9ac9a6b47f8ac4c6585348531f0ee4eb6a04c"
        val HEX = "0123456789abcdef".toCharArray()
    }
}
