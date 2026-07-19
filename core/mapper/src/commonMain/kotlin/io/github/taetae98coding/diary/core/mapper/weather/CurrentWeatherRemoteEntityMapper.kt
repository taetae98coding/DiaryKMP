package io.github.taetae98coding.diary.core.mapper.weather

import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.weather.network.api.entity.CurrentWeatherRemoteEntity

public fun CurrentWeatherRemoteEntity.toDomain(): Weather =
    Weather(
        dateTime = dateTime,
        conditionList = weather.map { remote -> remote.toDomain() },
        temperature = main.toDomain(),
    )
