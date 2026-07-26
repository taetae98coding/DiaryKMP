package io.github.taetae98coding.diary.feature.login.ui.credential

internal open class AppleCredentialsException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)
