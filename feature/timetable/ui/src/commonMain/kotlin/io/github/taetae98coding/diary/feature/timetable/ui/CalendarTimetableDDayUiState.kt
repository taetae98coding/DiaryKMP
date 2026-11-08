package io.github.taetae98coding.diary.feature.timetable.ui

import io.github.taetae98coding.diary.core.model.dday.CalendarDDay

internal data class CalendarTimetableDDayUiState(
    val ddayList: List<CalendarDDay> = emptyList(),
)
