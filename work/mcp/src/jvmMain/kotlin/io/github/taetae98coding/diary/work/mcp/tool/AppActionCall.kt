package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.work.appaction.core.AppActionException

// 공통 동작은 인자 오류와 실패를 AppActionException으로 감싸 던지므로, 도구가 직접 실행할 때와 같은 실패 메시지가 나가도록 감싸기 전의 예외로 되돌린다.
internal suspend fun <T> appActionCall(block: suspend () -> T): T =
    try {
        block()
    } catch (exception: AppActionException.InvalidArgument) {
        throw McpToolArgumentException(message = exception.message, cause = exception)
    } catch (exception: AppActionException.Failure) {
        throw exception.cause ?: exception
    }
