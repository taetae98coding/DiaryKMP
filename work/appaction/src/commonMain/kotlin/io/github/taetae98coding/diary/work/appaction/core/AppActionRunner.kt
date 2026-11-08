package io.github.taetae98coding.diary.work.appaction.core

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

// 데스크톱 MCP 도구와 같은 실패 메시지를 내도록, 인자 오류는 메시지만 담고 앱 기능의 실패는 예외 이름과 메시지를 함께 담는다.
internal suspend fun <T> runAppAction(block: suspend () -> T): T =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: AppActionException) {
        throw exception
    } catch (throwable: Throwable) {
        throw AppActionException.Failure(message = throwable.toFailureMessage(), cause = throwable)
    }

private fun Throwable.toFailureMessage(): String {
    val name = this::class.simpleName.orEmpty()
    val detail = message.orEmpty()

    return if (detail.isBlank()) name else "$name: $detail"
}

internal suspend fun <Parameter, Output> FlowUseCase<Parameter, Output>.readFirst(parameter: Parameter): Output = invoke(parameter = parameter).first().getOrThrow()
