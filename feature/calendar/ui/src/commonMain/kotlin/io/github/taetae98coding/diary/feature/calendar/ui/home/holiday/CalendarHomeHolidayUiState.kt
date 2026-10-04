package io.github.taetae98coding.diary.feature.calendar.ui.home.holiday

import io.github.taetae98coding.diary.core.model.holiday.Holiday

internal data class CalendarHomeHolidayUiState(
    val holidayList: List<Holiday> = emptyList(),
    val isFetching: Boolean = false,
)
