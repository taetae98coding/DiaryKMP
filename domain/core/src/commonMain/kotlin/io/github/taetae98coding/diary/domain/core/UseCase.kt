package io.github.taetae98coding.diary.domain.core

import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import kotlinx.coroutines.CancellationException

public abstract class UseCase<in Parameter, out Output> {
    public suspend operator fun invoke(parameter: Parameter): Result<Output> =
        try {
            Result.success(execute(parameter))
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            DiaryLogger.log(log = ConsoleLog(tag = this::class.simpleName.orEmpty(), message = "UseCase 실패", throwable = throwable))
            onFailure(throwable = throwable)
            Result.failure(throwable)
        }

    protected abstract suspend fun execute(parameter: Parameter): Output

    protected open fun onFailure(throwable: Throwable): Unit = Unit
}
