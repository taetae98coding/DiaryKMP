package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport

internal data class CalendarTimetableWeatherUiState(
    val weatherReport: CalendarWeatherReport = CalendarWeatherReport(),
)
