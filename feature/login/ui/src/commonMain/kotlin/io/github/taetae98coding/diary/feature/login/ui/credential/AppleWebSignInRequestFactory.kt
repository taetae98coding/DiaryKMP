package io.github.taetae98coding.diary.feature.login.ui.credential

import kotlin.uuid.Uuid

internal class AppleWebSignInRequestFactory(
    private val config: AppleCredentialsConfig,
) {
    fun create(returnUri: String): AppleWebSignInRequest {
        val nonce = Uuid.random().toString()
        val state = AppleWebSignInState.encode(token = Uuid.random().toString(), returnUri = returnUri)
        val query =
            listOf(
                "client_id" to config.clientId,
                "redirect_uri" to config.callbackUrl,
                "response_type" to RESPONSE_TYPE,
                "response_mode" to RESPONSE_MODE,
                "scope" to SCOPE,
                "state" to state,
                "nonce" to CredentialsNonce.hash(nonce),
            ).joinToString(separator = "&") { (name, value) -> "$name=${value.encodeUrlQueryComponent()}" }

        return AppleWebSignInRequest(
            authorizationUrl = "$APPLE_AUTHORIZATION_ENDPOINT?$query",
            state = state,
            nonce = nonce,
        )
    }

    private companion object {
        private const val APPLE_AUTHORIZATION_ENDPOINT = "https://appleid.apple.com/auth/authorize"

        // id_token을 앱이 직접 받으려면 response_type에 id_token을 넣어야 하고, 그 경우 Apple은 form_post만 허용한다.
        private const val RESPONSE_TYPE = "code id_token"
        private const val RESPONSE_MODE = "form_post"
        private const val SCOPE = "email"
    }
}
