package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport

internal data class CalendarHomeWeatherUiState(
    val weatherReport: CalendarWeatherReport = CalendarWeatherReport(),
    val isLoading: Boolean = false,
)
