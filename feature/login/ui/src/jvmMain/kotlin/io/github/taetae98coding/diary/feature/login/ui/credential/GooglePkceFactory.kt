package io.github.taetae98coding.diary.feature.login.ui.credential

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal class GooglePkceFactory(
    private val fillRandomBytes: (ByteArray) -> Unit = SecureRandom()::nextBytes,
) {
    fun create(): GooglePkce {
        val randomBytes = ByteArray(RANDOM_BYTE_COUNT)
        fillRandomBytes(randomBytes)

        val codeVerifier = BASE64_URL_ENCODER.encodeToString(randomBytes)
        val codeChallenge =
            BASE64_URL_ENCODER.encodeToString(
                MessageDigest
                    .getInstance(SHA_256_ALGORITHM)
                    .digest(codeVerifier.encodeToByteArray()),
            )

        return GooglePkce(
            codeVerifier = codeVerifier,
            codeChallenge = codeChallenge,
        )
    }

    private companion object {
        private const val RANDOM_BYTE_COUNT = 32
        private const val SHA_256_ALGORITHM = "SHA-256"
        private val BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding()
    }
}
