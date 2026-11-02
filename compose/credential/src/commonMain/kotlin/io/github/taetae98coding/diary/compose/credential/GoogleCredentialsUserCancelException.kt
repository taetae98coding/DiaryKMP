package io.github.taetae98coding.diary.compose.credential

public class GoogleCredentialsUserCancelException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : GoogleCredentialsException(message, cause)
