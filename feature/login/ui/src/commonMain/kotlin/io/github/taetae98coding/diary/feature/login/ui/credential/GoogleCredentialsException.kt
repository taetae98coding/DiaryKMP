package io.github.taetae98coding.diary.feature.login.ui.credential

import kotlin.coroutines.cancellation.CancellationException

internal open class GoogleCredentialsException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)

internal fun Throwable.toGoogleCredentialsFailure(): Throwable =
    when (this) {
        is CancellationException, is GoogleCredentialsException -> this
        else -> GoogleCredentialsException(cause = this)
    }
