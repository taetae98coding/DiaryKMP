package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import io.github.taetae98coding.diary.core.model.memo.CalendarMemo

internal data class CalendarTimetableMemoUiState(
    val memoList: List<CalendarMemo> = emptyList(),
)
