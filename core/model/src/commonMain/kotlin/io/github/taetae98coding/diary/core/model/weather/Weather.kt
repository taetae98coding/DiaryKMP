package io.github.taetae98coding.diary.core.model.weather

import kotlin.time.Instant

public data class Weather(
    val dateTime: Instant,
    val conditionList: List<WeatherCondition>,
    val temperature: WeatherTemperature,
)
