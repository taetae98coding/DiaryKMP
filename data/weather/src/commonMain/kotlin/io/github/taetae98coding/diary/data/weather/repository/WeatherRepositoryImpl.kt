package io.github.taetae98coding.diary.data.weather.repository

import io.github.taetae98coding.diary.core.model.weather.WeatherReport
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.data.weather.cache.WeatherReportCache
import io.github.taetae98coding.diary.data.weather.mapper.toDomain
import io.github.taetae98coding.diary.data.weather.mapper.toLocationName
import io.github.taetae98coding.diary.data.weather.mapper.toWeatherReport
import io.github.taetae98coding.diary.domain.location.repository.LocationRepository
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
    private val locationRepository: LocationRepository,
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val weatherReportCache: WeatherReportCache,
    private val clock: Clock,
) : WeatherRepository {
    override suspend fun fetch() {
        if (!isIntervalElapsed()) return

        refresh()
    }

    override suspend fun refresh() {
        val location = locationRepository.fetch()

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

            weatherReportCache.update(
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

    override fun get(): Flow<WeatherReport?> = weatherReportCache.get().map { storedWeather -> storedWeather.toWeatherReport(forecastInterval = weatherRemoteDataSource.forecastInterval) }

    private fun isIntervalElapsed(): Boolean {
        val fetchedAt = weatherReportCache.getFetchedAt() ?: return true

        return FETCH_INTERVAL <= clock.now() - fetchedAt
    }
}
