package io.github.taetae98coding.diary.feature.login.ui.credential

import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential

internal class GoogleAuthorizationCodeAttempt(
    val authorizationUrl: String,
    private val clientId: String,
    private val redirectUri: String,
    private val codeVerifier: String,
) {
    fun toCredential(parameters: Map<String, String>): GoogleCredential.AuthorizationCode =
        GoogleCredential.AuthorizationCode(
            code =
                googleAuthorizationCodeOrThrow(
                    code = parameters[AuthorizationResponseParameter.CODE].orEmpty(),
                    error = parameters[AuthorizationResponseParameter.ERROR].orEmpty(),
                ),
            clientId = clientId,
            redirectUri = redirectUri,
            codeVerifier = codeVerifier,
        )
}
