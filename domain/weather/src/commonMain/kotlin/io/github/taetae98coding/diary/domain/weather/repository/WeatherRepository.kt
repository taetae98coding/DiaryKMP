package io.github.taetae98coding.diary.domain.weather.repository

import io.github.taetae98coding.diary.core.model.weather.WeatherReport
import kotlinx.coroutines.flow.Flow

public interface WeatherRepository {
    public suspend fun fetch()

    public suspend fun refresh()

    public fun get(): Flow<WeatherReport?>
}
