package io.github.taetae98coding.diary.core.weather.network.impl.datasource

import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.core.weather.network.api.entity.CurrentWeatherRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.ForecastRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.api.entity.LocationNameRemoteEntity
import io.github.taetae98coding.diary.core.weather.network.impl.di.WeatherHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.koin.core.annotation.Factory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

private const val LOCATION_NAME_LIMIT = 1

// 날씨 상태의 설명 문구는 사용자 언어와 관계없이 언제나 영어로 받는다.
private const val LANGUAGE = "en"

@Factory
internal class WeatherRemoteDataSourceImpl(
    @WeatherHttpClient
    private val httpClient: HttpClient,
) : WeatherRemoteDataSource {
    // data/2.5/forecast는 예보를 3시간 간격 구간으로 반환한다.
    override val forecastInterval: Duration = 3.hours

    override suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
    ): CurrentWeatherRemoteEntity =
        httpClient
            .get("data/2.5/weather") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("lang", LANGUAGE)
            }.body()

    override suspend fun getForecast(
        latitude: Double,
        longitude: Double,
    ): ForecastRemoteEntity =
        httpClient
            .get("data/2.5/forecast") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("lang", LANGUAGE)
            }.body()

    override suspend fun getLocationName(
        latitude: Double,
        longitude: Double,
    ): List<LocationNameRemoteEntity> =
        httpClient
            .get("geo/1.0/reverse") {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("limit", LOCATION_NAME_LIMIT)
            }.body()
}
