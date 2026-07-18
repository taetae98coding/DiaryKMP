package io.github.taetae98coding.diary.core.model.authentication

public sealed interface GoogleCredential {
    public data class IdToken(
        val idToken: String,
        val nonce: String,
    ) : GoogleCredential

    public data class AuthorizationCode(
        val code: String,
        val clientId: String,
        val redirectUri: String,
        val codeVerifier: String? = null,
    ) : GoogleCredential
}
