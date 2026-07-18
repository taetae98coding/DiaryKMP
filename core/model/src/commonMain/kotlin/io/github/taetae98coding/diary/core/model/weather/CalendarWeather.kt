package io.github.taetae98coding.diary.core.model.weather

import kotlinx.datetime.LocalDate

public data class CalendarWeather(
    val date: LocalDate,
    val temperature: CalendarWeatherTemperature,
    val weatherList: List<Weather>,
)
