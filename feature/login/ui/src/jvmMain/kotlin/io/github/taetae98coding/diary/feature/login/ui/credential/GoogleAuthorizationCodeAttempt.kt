package io.github.taetae98coding.diary.feature.login.ui.credential

import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential

internal class GoogleAuthorizationCodeAttempt(
    val authorizationUrl: String,
    private val clientId: String,
    private val redirectUri: String,
    private val codeVerifier: String,
) {
    fun credential(code: String): GoogleCredential.AuthorizationCode =
        GoogleCredential.AuthorizationCode(
            code = code,
            clientId = clientId,
            redirectUri = redirectUri,
            codeVerifier = codeVerifier,
        )
}
