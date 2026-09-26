package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalTime

public interface TimetableScope {
    public fun timeItem(
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
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
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
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
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        key: Any,
        content: @Composable () -> Unit,
    ) {
        timeItemList += TimetableTimeItem(date = date, startTime = startTime, endTime = endTime, key = key, content = content)
    }

    override fun allDayItem(
        dateRange: LocalDateRange,
        key: Any,
        content: @Composable () -> Unit,
    ) {
        allDayItemList += TimetableAllDayItem(dateRange = dateRange, key = key, content = content)
    }
}
