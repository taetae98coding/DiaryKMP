package io.github.taetae98coding.diary.compose.credential

public class AppleCredentialsUserCancelException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : AppleCredentialsException(message, cause)
