package io.github.taetae98coding.diary.feature.login.ui.credential

import java.security.SecureRandom
import kotlin.io.encoding.Base64

internal class GooglePkceFactory(
    private val fillRandomBytes: (ByteArray) -> Unit = SecureRandom()::nextBytes,
) {
    fun create(): GooglePkce {
        val randomBytes = ByteArray(RANDOM_BYTE_COUNT)
        fillRandomBytes(randomBytes)

        val codeVerifier = BASE64_URL_ENCODER.encode(randomBytes)
        val codeChallenge = BASE64_URL_ENCODER.encode(codeVerifier.encodeToByteArray().sha256())

        return GooglePkce(
            codeVerifier = codeVerifier,
            codeChallenge = codeChallenge,
        )
    }

    private companion object {
        private const val RANDOM_BYTE_COUNT = 32
        private val BASE64_URL_ENCODER = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
    }
}
