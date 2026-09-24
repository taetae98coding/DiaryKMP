package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class FetchHolidayUseCase internal constructor(
    private val getHolidayCountrySettingUseCase: GetHolidayCountrySettingUseCase,
    private val holidayRepository: HolidayRepository,
) : UseCase<Int, List<Holiday>>() {
    override suspend fun execute(parameter: Int): List<Holiday> {
        val countrySet = getHolidayCountrySettingUseCase.countrySetFlow().first()

        // 한 국가가 실패해도 나머지 국가를 끝까지 받은 뒤 첫 실패를 전달한다.
        return countrySet
            .map { country -> fetchCatching { holidayRepository.fetch(country = country, year = parameter) } }
            .flatMap { result -> result.getOrThrow() }
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = CrashlyticsLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}

private suspend fun fetchCatching(block: suspend () -> List<Holiday>): Result<List<Holiday>> =
    try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
