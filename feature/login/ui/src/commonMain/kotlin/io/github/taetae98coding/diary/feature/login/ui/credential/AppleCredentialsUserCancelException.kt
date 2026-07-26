package io.github.taetae98coding.diary.feature.login.ui.credential

internal class AppleCredentialsUserCancelException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : AppleCredentialsException(message, cause)
