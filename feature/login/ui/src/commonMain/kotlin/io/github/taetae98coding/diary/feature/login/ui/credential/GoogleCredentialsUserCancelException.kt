package io.github.taetae98coding.diary.feature.login.ui.credential

internal class GoogleCredentialsUserCancelException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : GoogleCredentialsException(message, cause)
