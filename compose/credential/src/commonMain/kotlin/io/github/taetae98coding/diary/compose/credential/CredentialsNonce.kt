package io.github.taetae98coding.diary.compose.credential

internal object CredentialsNonce {
    fun hash(nonce: String): String =
        nonce
            .encodeToByteArray()
            .sha256()
            .toHexString()
}
