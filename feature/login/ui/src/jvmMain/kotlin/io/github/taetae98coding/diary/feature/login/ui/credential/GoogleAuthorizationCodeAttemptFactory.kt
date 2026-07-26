package io.github.taetae98coding.diary.feature.login.ui.credential

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl

internal class GoogleAuthorizationCodeAttemptFactory(
    private val clientId: String,
    private val googlePkceFactory: GooglePkceFactory = GooglePkceFactory(),
) {
    fun create(redirectUri: String): GoogleAuthorizationCodeAttempt {
        val pkce = googlePkceFactory.create()
        val authorizationUrl =
            AuthorizationCodeRequestUrl(GOOGLE_AUTHORIZATION_ENDPOINT, clientId)
                .apply {
                    setRedirectUri(redirectUri)
                    setScopes(GOOGLE_AUTHENTICATION_SCOPES)
                    setCodeChallenge(pkce.codeChallenge)
                    setCodeChallengeMethod(PKCE_CODE_CHALLENGE_METHOD)
                }.build()

        return GoogleAuthorizationCodeAttempt(
            authorizationUrl = authorizationUrl,
            clientId = clientId,
            redirectUri = redirectUri,
            codeVerifier = pkce.codeVerifier,
        )
    }

    private companion object {
        private const val GOOGLE_AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val PKCE_CODE_CHALLENGE_METHOD = "S256"
        private val GOOGLE_AUTHENTICATION_SCOPES = listOf("openid", "profile", "email")
    }
}
