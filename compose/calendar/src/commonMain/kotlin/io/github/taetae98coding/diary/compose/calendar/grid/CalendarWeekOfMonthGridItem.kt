package io.github.taetae98coding.diary.compose.calendar.grid

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDateRange

internal data class CalendarWeekOfMonthGridItem(
    val key: Any?,
    val dateRange: LocalDateRange,
    val content: @Composable (CalendarWeekOfMonthGridItemScope.() -> Unit),
)
