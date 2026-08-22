package io.github.taetae98coding.diary.domain.weather.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import org.koin.core.annotation.Factory

@Factory
public class RefreshCurrentWeatherUseCase internal constructor(
    private val weatherRepository: WeatherRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        weatherRepository.refresh()
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = CrashlyticsLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}
