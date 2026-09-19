package io.github.taetae98coding.diary.compose.calendar

import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayBasedNumber
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

internal const val WEEKS_PER_MONTH: Int = 6

public fun YearMonth.weekOfMonthAt(date: LocalDate): Int = (firstDay.daysUntil(date) + firstDay.dayOfWeek.sundayBasedNumber) / DAYS_PER_WEEK

internal fun YearMonth.dateAt(
    weekOfMonth: Int,
    dayOfWeek: DayOfWeek,
): LocalDate {
    val dayOffset = weekOfMonth * DAYS_PER_WEEK + dayOfWeek.sundayBasedNumber - firstDay.dayOfWeek.sundayBasedNumber

    return firstDay.plus(dayOffset, DateTimeUnit.DAY)
}

internal fun YearMonth.dateRangeAt(weekOfMonth: Int): LocalDateRange =
    LocalDateRange(
        start = dateAt(weekOfMonth = weekOfMonth, dayOfWeek = DayOfWeek.SUNDAY),
        endInclusive = dateAt(weekOfMonth = weekOfMonth, dayOfWeek = DayOfWeek.SATURDAY),
    )
