package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime

public interface TimetableScope {
    public fun timeItem(
        start: LocalDateTime,
        endInclusive: LocalDateTime,
        key: Any,
        content: @Composable () -> Unit,
    )

    public fun allDayItem(
        dateRange: LocalDateRange,
        key: Any,
        content: @Composable () -> Unit,
    )
}

internal data class TimetableTimeItem(
    val start: LocalDateTime,
    val endInclusive: LocalDateTime,
    val key: Any,
    val content: @Composable () -> Unit,
)

internal data class TimetableAllDayItem(
    val dateRange: LocalDateRange,
    val key: Any,
    val content: @Composable () -> Unit,
)

internal class TimetableScopeImpl : TimetableScope {
    val timeItemList = mutableListOf<TimetableTimeItem>()
    val allDayItemList = mutableListOf<TimetableAllDayItem>()

    override fun timeItem(
        start: LocalDateTime,
        endInclusive: LocalDateTime,
        key: Any,
        content: @Composable () -> Unit,
    ) {
        timeItemList += TimetableTimeItem(start = start, endInclusive = endInclusive, key = key, content = content)
    }

    override fun allDayItem(
        dateRange: LocalDateRange,
        key: Any,
        content: @Composable () -> Unit,
    ) {
        allDayItemList += TimetableAllDayItem(dateRange = dateRange, key = key, content = content)
    }
}
