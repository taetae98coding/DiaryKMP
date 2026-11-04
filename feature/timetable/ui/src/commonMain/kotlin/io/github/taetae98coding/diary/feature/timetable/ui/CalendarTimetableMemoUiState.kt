package io.github.taetae98coding.diary.feature.timetable.ui

import io.github.taetae98coding.diary.core.model.memo.CalendarMemo

internal data class CalendarTimetableMemoUiState(
    val memoList: List<CalendarMemo> = emptyList(),
)
