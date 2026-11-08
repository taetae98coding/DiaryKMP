package io.github.taetae98coding.diary.feature.calendar.ui.home.dday

import io.github.taetae98coding.diary.core.model.dday.CalendarDDay

internal data class CalendarHomeDDayUiState(
    val ddayList: List<CalendarDDay> = emptyList(),
)
