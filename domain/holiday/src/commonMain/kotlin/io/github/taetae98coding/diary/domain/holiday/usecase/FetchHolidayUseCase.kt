package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import org.koin.core.annotation.Factory

@Factory
public class FetchHolidayUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
) : UseCase<Int, List<Holiday>>() {
    override suspend fun execute(parameter: Int): List<Holiday> = holidayRepository.fetch(year = parameter)

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = CrashlyticsLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}
