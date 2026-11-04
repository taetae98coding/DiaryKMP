package io.github.taetae98coding.diary.feature.timetable.ui

import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport

internal data class CalendarTimetableWeatherUiState(
    val weatherReport: CalendarWeatherReport = CalendarWeatherReport(),
)
