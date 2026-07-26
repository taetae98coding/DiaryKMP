package io.github.taetae98coding.diary.domain.core

import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

public abstract class FlowUseCase<in Parameter, out Output> {
    public operator fun invoke(parameter: Parameter): Flow<Result<Output>> =
        flow {
            emitAll(execute(parameter))
        }.catch { throwable ->
            DiaryLogger.log(log = ConsoleLog(tag = this@FlowUseCase::class.simpleName.orEmpty(), message = "UseCase 실패", throwable = throwable))
            emit(Result.failure(throwable))
        }

    protected abstract fun execute(parameter: Parameter): Flow<Result<Output>>
}
