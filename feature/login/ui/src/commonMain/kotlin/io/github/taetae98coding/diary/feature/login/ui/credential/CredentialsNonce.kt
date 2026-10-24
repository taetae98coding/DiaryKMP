package io.github.taetae98coding.diary.feature.login.ui.credential

internal object CredentialsNonce {
    fun hash(nonce: String): String =
        nonce
            .encodeToByteArray()
            .sha256()
            .toHexString()
}
