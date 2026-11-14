package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord

internal data class RoutineHomeCalendarUiState(
    val recordList: List<CalendarRoutineRecord> = emptyList(),
)
