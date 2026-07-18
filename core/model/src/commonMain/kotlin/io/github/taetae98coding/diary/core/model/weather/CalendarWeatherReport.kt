package io.github.taetae98coding.diary.core.model.weather

public data class CalendarWeatherReport(
    val weatherList: List<CalendarWeather> = emptyList(),
    val locationName: String = "",
)
