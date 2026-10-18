package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import io.github.taetae98coding.diary.core.model.holiday.Holiday

internal data class CalendarTimetableHolidayUiState(
    val holidayList: List<Holiday> = emptyList(),
)
