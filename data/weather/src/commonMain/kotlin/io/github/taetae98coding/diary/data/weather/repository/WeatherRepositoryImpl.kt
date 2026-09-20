package io.github.taetae98coding.diary.data.weather.repository

import io.github.taetae98coding.diary.core.ipnetwork.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.model.weather.WeatherReport
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.data.weather.datasource.WeatherLocalDataSource
import io.github.taetae98coding.diary.data.weather.mapper.toDomain
import io.github.taetae98coding.diary.data.weather.mapper.toLocationName
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

private val FETCH_INTERVAL: Duration = 1.hours

@Factory
internal class WeatherRepositoryImpl(
    private val locationProvider: LocationProvider,
    private val ipRemoteDataSource: IpRemoteDataSource,
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val weatherLocalDataSource: WeatherLocalDataSource,
    private val clock: Clock,
) : WeatherRepository {
    override suspend fun fetch() {
        if (!isIntervalElapsed()) return

        refresh()
    }

    override suspend fun refresh() {
        val location = getLocation()

        coroutineScope {
            val currentWeather =
                async {
                    weatherRemoteDataSource.getCurrentWeather(
                        latitude = location.latitude,
                        longitude = location.longitude,
                    )
                }
            val forecast =
                async {
                    weatherRemoteDataSource.getForecast(
                        latitude = location.latitude,
                        longitude = location.longitude,
                    )
                }
            val locationName =
                async {
                    try {
                        weatherRemoteDataSource
                            .getLocationName(
                                latitude = location.latitude,
                                longitude = location.longitude,
                            ).toLocationName()
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Throwable) {
                        ""
                    }
                }

            weatherLocalDataSource.update(
                weatherList =
                    buildList {
                        add(currentWeather.await().toDomain())
                        addAll(forecast.await().list.map { remote -> remote.toDomain() })
                    },
                locationName = locationName.await(),
                fetchedAt = clock.now(),
            )
        }
    }

    override fun get(): Flow<WeatherReport?> = weatherLocalDataSource.get().map { storedWeather -> storedWeather.toWeatherReport() }

    private fun isIntervalElapsed(): Boolean {
        val fetchedAt = weatherLocalDataSource.getFetchedAt() ?: return true

        return FETCH_INTERVAL <= clock.now() - fetchedAt
    }

    private fun WeatherLocalDataSource.StoredWeather.toWeatherReport(): WeatherReport? {
        if (weatherList.isEmpty()) return null

        return WeatherReport(
            weatherList = weatherList,
            coverage =
                weatherList.minOf { weather -> weather.dateTime }..<weatherList.maxOf { weather -> weather.dateTime } + weatherRemoteDataSource.forecastInterval,
            locationName = locationName,
        )
    }

    private suspend fun getLocation(): Location =
        locationProvider
            .getCurrentLocation()
            ?.let { deviceLocation -> Location(latitude = deviceLocation.latitude, longitude = deviceLocation.longitude) }
            ?: ipRemoteDataSource.get().let { entity -> Location(latitude = entity.latitude, longitude = entity.longitude) }

    private data class Location(
        val latitude: Double,
        val longitude: Double,
    )
}
