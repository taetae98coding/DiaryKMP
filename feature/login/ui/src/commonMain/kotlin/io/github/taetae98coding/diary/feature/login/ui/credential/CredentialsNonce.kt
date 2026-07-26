package io.github.taetae98coding.diary.feature.login.ui.credential

import org.kotlincrypto.hash.sha2.SHA256

internal object CredentialsNonce {
    fun hash(nonce: String): String =
        SHA256()
            .digest(nonce.encodeToByteArray())
            .toHexString()
}
