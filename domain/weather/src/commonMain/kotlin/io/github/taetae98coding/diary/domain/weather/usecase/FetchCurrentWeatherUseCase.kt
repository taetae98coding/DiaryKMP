package io.github.taetae98coding.diary.domain.weather.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository

@Inject
public class FetchCurrentWeatherUseCase internal constructor(
    private val weatherRepository: WeatherRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        weatherRepository.fetch()
    }
}
