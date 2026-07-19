package io.github.taetae98coding.diary.core.mapper.weather

import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherMainRemoteEntity

public fun WeatherMainRemoteEntity.toDomain(): WeatherTemperature =
    WeatherTemperature(
        current = temp,
        min = tempMin,
        max = tempMax,
    )
