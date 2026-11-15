package io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday

import io.github.taetae98coding.diary.core.model.holiday.Holiday

internal data class RoutineHomeHolidayUiState(
    val holidayList: List<Holiday> = emptyList(),
    val isFetching: Boolean = false,
)
