package io.github.taetae98coding.diary.feature.login.ui.credential

internal const val GOOGLE_AUTHORIZATION_SCOPE: String = "openid profile email"

// RFC 6749 4.1.2.1: 사용자가 동의를 거부하면 Google은 인가 코드 대신 access_denied 오류를 돌려준다.
private const val ACCESS_DENIED_ERROR = "access_denied"

internal fun googleAuthorizationCodeOrThrow(
    code: String,
    error: String,
): String =
    when {
        code.isNotEmpty() -> code
        error == ACCESS_DENIED_ERROR -> throw GoogleCredentialsUserCancelException(message = error)
        else -> throw GoogleCredentialsException(message = error.ifEmpty { "no authorization code" })
    }
