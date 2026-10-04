package io.github.taetae98coding.diary.domain.weather.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.logCrashlyticsFailure
import org.koin.core.annotation.Factory

@Factory
public class FetchCurrentWeatherUseCase internal constructor(
    private val weatherRepository: WeatherRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        weatherRepository.fetch()
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.logCrashlyticsFailure(source = this, throwable = throwable)
    }
}
