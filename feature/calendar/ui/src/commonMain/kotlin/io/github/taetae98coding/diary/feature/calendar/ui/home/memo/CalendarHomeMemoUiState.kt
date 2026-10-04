package io.github.taetae98coding.diary.feature.calendar.ui.home.memo

import io.github.taetae98coding.diary.core.model.memo.CalendarMemo

internal data class CalendarHomeMemoUiState(
    val memoList: List<CalendarMemo> = emptyList(),
)
