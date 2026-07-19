package io.github.taetae98coding.diary.core.mapper.weather

import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.weather.network.api.entity.WeatherConditionRemoteEntity

private const val OPEN_WEATHER_ICON_BASE_URL = "https://openweathermap.org/img/wn"
private const val DAY_ICON_SUFFIX = "d"
private const val NIGHT_ICON_SUFFIX = "n"

public fun WeatherConditionRemoteEntity.toDomain(): WeatherCondition =
    WeatherCondition(
        description = description,
        imageUrl = "$OPEN_WEATHER_ICON_BASE_URL/${icon.toDayIcon()}.png",
    )

private fun String.toDayIcon(): String =
    if (endsWith(suffix = NIGHT_ICON_SUFFIX)) {
        removeSuffix(suffix = NIGHT_ICON_SUFFIX) + DAY_ICON_SUFFIX
    } else {
        this
    }
