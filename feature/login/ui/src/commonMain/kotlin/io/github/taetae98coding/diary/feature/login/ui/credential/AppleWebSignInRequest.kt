package io.github.taetae98coding.diary.feature.login.ui.credential

import io.github.taetae98coding.diary.core.model.authentication.AppleCredential

internal class AppleWebSignInRequest(
    val authorizationUrl: String,
    val state: String,
    private val nonce: String,
) {
    fun toCredential(response: AppleWebSignInResponse): AppleCredential {
        val idToken = response.idToken

        return if (response.state == state && idToken != null) {
            AppleCredential(idToken = idToken, nonce = nonce)
        } else {
            throw response.toFailure()
        }
    }

    private fun AppleWebSignInResponse.toFailure(): AppleCredentialsException =
        when {
            state != this@AppleWebSignInRequest.state -> AppleCredentialsException(message = "state mismatch")
            error == USER_CANCELLED_ERROR -> AppleCredentialsUserCancelException(message = error)
            else -> AppleCredentialsException(message = error ?: "no identity token")
        }

    private companion object {
        private const val USER_CANCELLED_ERROR = "user_cancelled_authorize"
    }
}
