package com.example.core

import com.example.data.model.PartnerRole
import java.security.MessageDigest

/**
 * Verifies the 6-digit secret access code using SHA-256 digest comparison.
 * Codes are never stored or displayed in plaintext UI.
 */
object SecretCodeAuth {
    private const val DIGEST_ME = "e9d06cc9976c3e99cfb527deb1a939b0e1e042092af9a34b988672bd0ff1ee9b"
    private const val DIGEST_UBAID = "7dc2a2e51db47c3013746c5cf99130da84e875f43881dc10f461f6251a472c17"

    fun verifyAccessCode(inputCode: String): PartnerRole? {
        val trimmed = inputCode.trim()
        if (trimmed.length != 6 || !trimmed.all { it.isDigit() }) return null
        val digest = sha256Hex(trimmed)
        return when (digest) {
            DIGEST_ME -> PartnerRole.ME
            DIGEST_UBAID -> PartnerRole.UBAID
            else -> null
        }
    }

    private fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(("twominds_salt_v1:$input").toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
