package io.github.taetae98coding.diary.domain.weather.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
import org.koin.core.annotation.Factory

@Factory
public class RefreshCurrentWeatherUseCase internal constructor(
    private val weatherRepository: WeatherRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        weatherRepository.refresh()
    }
}
