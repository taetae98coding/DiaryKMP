package io.github.taetae98coding.diary.work.appaction.appfunction.core

import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import io.github.taetae98coding.diary.work.appaction.core.AppActionException

internal suspend fun <T> appFunctionCall(block: suspend () -> T): T =
    try {
        block()
    } catch (exception: AppActionException.InvalidArgument) {
        throw AppFunctionInvalidArgumentException(exception.message).apply { initCause(exception) }
    } catch (exception: AppActionException.Failure) {
        throw AppFunctionAppUnknownException(exception.message).apply { initCause(exception) }
    }
