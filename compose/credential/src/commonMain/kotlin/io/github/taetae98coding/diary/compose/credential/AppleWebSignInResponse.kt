package io.github.taetae98coding.diary.compose.credential

internal data class AppleWebSignInResponse(
    val idToken: String?,
    val code: String?,
    val state: String?,
    val error: String?,
) {
    companion object {
        fun fromParameters(getParameter: (String) -> String?): AppleWebSignInResponse =
            AppleWebSignInResponse(
                idToken = getParameter(AuthorizationResponseParameter.ID_TOKEN),
                code = getParameter(AuthorizationResponseParameter.CODE),
                state = getParameter(AuthorizationResponseParameter.STATE),
                error = getParameter(AuthorizationResponseParameter.ERROR),
            )
    }
}
