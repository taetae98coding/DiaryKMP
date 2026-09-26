package io.github.taetae98coding.diary.compose.timetable

import io.github.taetae98coding.diary.library.kotlinx.datetime.overlaps
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalTime

internal const val MINUTES_PER_HOUR: Int = 60
internal const val HOURS_PER_DAY: Int = 24
internal const val MINUTES_PER_DAY: Int = MINUTES_PER_HOUR * HOURS_PER_DAY
internal const val MIN_TIME_ITEM_MINUTES: Int = 30
internal const val INITIAL_SCROLL_HOUR: Int = 8

internal data class TimetablePlacedTimeItem(
    val item: TimetableTimeItem,
    val startMinute: Int,
    val endMinute: Int,
    val column: Int,
    val columnCount: Int,
)

internal data class TimetablePlacedAllDayItem(
    val item: TimetableAllDayItem,
    val dateRange: LocalDateRange,
    val row: Int,
)

internal fun List<TimetableTimeItem>.placeOn(date: LocalDate): List<TimetablePlacedTimeItem> {
    val spanList =
        filter { item -> item.date == date }
            .map { item -> item.span() }
            .sortedWith(compareBy<TimeSpan> { it.startMinute }.thenByDescending { it.endMinute })
    val result = mutableListOf<TimetablePlacedTimeItem>()
    val cluster = mutableListOf<Pair<TimeSpan, Int>>()
    val laneEndList = mutableListOf<Int>()
    var clusterEnd = Int.MIN_VALUE

    fun flushCluster() {
        cluster.forEach { (span, column) ->
            result +=
                TimetablePlacedTimeItem(
                    item = span.item,
                    startMinute = span.startMinute,
                    endMinute = span.endMinute,
                    column = column,
                    columnCount = laneEndList.size,
                )
        }
        cluster.clear()
        laneEndList.clear()
    }

    spanList.forEach { span ->
        if (span.startMinute >= clusterEnd) {
            flushCluster()
        }

        val column = laneEndList.indexOfFirst { laneEnd -> laneEnd <= span.startMinute }
        val placedColumn =
            if (column < 0) {
                laneEndList += span.endMinute
                laneEndList.lastIndex
            } else {
                laneEndList[column] = span.endMinute
                column
            }

        cluster += span to placedColumn
        clusterEnd = maxOf(clusterEnd, span.endMinute)
    }
    flushCluster()

    return result
}

internal fun List<TimetableAllDayItem>.placeIn(dateRange: LocalDateRange): List<TimetablePlacedAllDayItem> {
    val spanList =
        filter { item -> item.dateRange overlaps dateRange }
            .map { item ->
                AllDaySpan(
                    item = item,
                    dateRange = maxOf(item.dateRange.start, dateRange.start)..minOf(item.dateRange.endInclusive, dateRange.endInclusive),
                )
            }.sortedWith(
                compareBy<AllDaySpan> { it.dateRange.start }
                    .thenByDescending { it.dateRange.endInclusive }
                    .thenBy { it.item.dateRange.start }
                    .thenByDescending { it.item.dateRange.endInclusive },
            )
    val rowEndList = mutableListOf<LocalDate>()

    return spanList.map { span ->
        val row = rowEndList.indexOfFirst { rowEnd -> rowEnd < span.dateRange.start }
        val placedRow =
            if (row < 0) {
                rowEndList += span.dateRange.endInclusive
                rowEndList.lastIndex
            } else {
                rowEndList[row] = span.dateRange.endInclusive
                row
            }

        TimetablePlacedAllDayItem(
            item = span.item,
            dateRange = span.dateRange,
            row = placedRow,
        )
    }
}

internal val LocalTime.minuteOfDay: Int
    get() = hour * MINUTES_PER_HOUR + minute

private fun TimetableTimeItem.span(): TimeSpan {
    val startMinute = startTime.minuteOfDay
    val endMinute = maxOf(endTime.minuteOfDay, startMinute + MIN_TIME_ITEM_MINUTES)

    return if (endMinute > MINUTES_PER_DAY) {
        TimeSpan(item = this, startMinute = MINUTES_PER_DAY - MIN_TIME_ITEM_MINUTES, endMinute = MINUTES_PER_DAY)
    } else {
        TimeSpan(item = this, startMinute = startMinute, endMinute = endMinute)
    }
}

private data class TimeSpan(
    val item: TimetableTimeItem,
    val startMinute: Int,
    val endMinute: Int,
)

private data class AllDaySpan(
    val item: TimetableAllDayItem,
    val dateRange: LocalDateRange,
)
