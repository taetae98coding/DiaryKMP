package io.github.taetae98coding.diary.core.weather.network.api.datasource

import io.github.taetae98coding.diary.core.weather.network.api.entity.CurrentWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.LocationNameRemoteEntity
import kotlin.time.Duration

public interface WeatherRemoteDataSource {
    public val forecastInterval: Duration

    public suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
    ): CurrentWeatherRemoteEntity

    public suspend fun getForecast(
        latitude: Double,
        longitude: Double,
    ): ForecastRemoteEntity

    public suspend fun getLocationName(
        latitude: Double,
        longitude: Double,
    ): List<LocationNameRemoteEntity>
}
