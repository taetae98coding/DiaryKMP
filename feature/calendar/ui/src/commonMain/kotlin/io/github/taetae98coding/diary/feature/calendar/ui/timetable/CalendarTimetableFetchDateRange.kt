package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.plus

internal fun LocalDateRange.calendarTimetableFetchDateRange(): LocalDateRange {
    val dayCount = count()

    return start.minus(dayCount, DateTimeUnit.DAY)..endInclusive.plus(dayCount, DateTimeUnit.DAY)
}
