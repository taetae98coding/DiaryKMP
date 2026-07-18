package io.github.taetae98coding.diary.core.model.weather

import kotlin.time.Instant

public data class WeatherReport(
    val weatherList: List<Weather>,
    val coverage: OpenEndRange<Instant>,
    val locationName: String,
)
