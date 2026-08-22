@file:OptIn(ExperimentalAtomicApi::class)

package io.github.taetae98coding.diary.data.weather.datasource

import io.github.taetae98coding.diary.core.model.weather.Weather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Instant

@Single
internal class WeatherLocalDataSource {
    private val storedWeather = MutableStateFlow(StoredWeather())
    private val fetchedAt = AtomicReference<Instant?>(null)

    fun get(): Flow<StoredWeather> = storedWeather.asStateFlow()

    fun getFetchedAt(): Instant? = fetchedAt.load()

    fun update(
        weatherList: List<Weather>,
        locationName: String,
        fetchedAt: Instant,
    ) {
        storedWeather.value =
            StoredWeather(
                weatherList = weatherList,
                locationName = locationName,
            )
        this.fetchedAt.store(fetchedAt)
    }

    data class StoredWeather(
        val weatherList: List<Weather> = emptyList(),
        val locationName: String = "",
    )
}
