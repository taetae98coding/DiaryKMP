package io.github.taetae98coding.diary.feature.login.ui.credential

internal class GoogleAuthorizationCodeAttemptFactory(
    private val clientId: String,
    private val googlePkceFactory: GooglePkceFactory = GooglePkceFactory(),
) {
    fun create(redirectUri: String): GoogleAuthorizationCodeAttempt {
        val pkce = googlePkceFactory.create()
        val authorizationUrl =
            GOOGLE_AUTHORIZATION_ENDPOINT.withUrlQuery(
                listOf(
                    "client_id" to clientId,
                    "redirect_uri" to redirectUri,
                    "response_type" to RESPONSE_TYPE,
                    "scope" to GOOGLE_AUTHORIZATION_SCOPE,
                    "code_challenge" to pkce.codeChallenge,
                    "code_challenge_method" to PKCE_CODE_CHALLENGE_METHOD,
                ),
            )

        return GoogleAuthorizationCodeAttempt(
            authorizationUrl = authorizationUrl,
            clientId = clientId,
            redirectUri = redirectUri,
            codeVerifier = pkce.codeVerifier,
        )
    }

    private companion object {
        private const val GOOGLE_AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val RESPONSE_TYPE = "code"
        private const val PKCE_CODE_CHALLENGE_METHOD = "S256"
    }
}
