package io.github.taetae98coding.diary.feature.login.ui.credential

import kotlin.coroutines.cancellation.CancellationException

internal open class AppleCredentialsException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)

// 취소와 이미 분류된 실패는 그대로 두고, 플랫폼이 던진 나머지 예외만 인증 결과 획득 실패로 감싼다.
internal fun Throwable.toAppleCredentialsFailure(): Throwable =
    when (this) {
        is CancellationException, is AppleCredentialsException -> this
        else -> AppleCredentialsException(cause = this)
    }
