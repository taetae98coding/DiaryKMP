package io.github.taetae98coding.diary.feature.login.ui.credential

internal data class AppleWebSignInResponse(
    val idToken: String?,
    val state: String?,
    val error: String?,
) {
    companion object {
        fun fromParameters(getParameter: (String) -> String?): AppleWebSignInResponse =
            AppleWebSignInResponse(
                idToken = getParameter(AuthorizationResponseParameter.ID_TOKEN),
                state = getParameter(AuthorizationResponseParameter.STATE),
                error = getParameter(AuthorizationResponseParameter.ERROR),
            )
    }
}
