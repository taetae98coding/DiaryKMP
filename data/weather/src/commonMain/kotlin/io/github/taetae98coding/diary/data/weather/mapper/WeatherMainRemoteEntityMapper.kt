package io.github.taetae98coding.diary.data.weather.mapper

import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherMainRemoteEntity

internal fun WeatherMainRemoteEntity.toDomain(): WeatherTemperature =
    WeatherTemperature(
        current = temp,
        min = tempMin,
        max = tempMax,
    )
