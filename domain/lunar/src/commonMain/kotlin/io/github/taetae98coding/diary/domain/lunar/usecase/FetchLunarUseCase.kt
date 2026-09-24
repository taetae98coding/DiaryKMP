package io.github.taetae98coding.diary.domain.lunar.usecase

import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import org.koin.core.annotation.Factory

@Factory
public class FetchLunarUseCase internal constructor(
    private val lunarRepository: LunarRepository,
) : UseCase<Int, List<LunarDate>>() {
    override suspend fun execute(parameter: Int): List<LunarDate> = lunarRepository.fetch(year = parameter)

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = CrashlyticsLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}
