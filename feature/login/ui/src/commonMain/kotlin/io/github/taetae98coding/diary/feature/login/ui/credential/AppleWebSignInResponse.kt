package io.github.taetae98coding.diary.feature.login.ui.credential

internal data class AppleWebSignInResponse(
    val idToken: String?,
    val state: String?,
    val error: String?,
) {
    companion object {
        const val ID_TOKEN_PARAMETER = "id_token"
        const val STATE_PARAMETER = "state"
        const val ERROR_PARAMETER = "error"
    }
}
