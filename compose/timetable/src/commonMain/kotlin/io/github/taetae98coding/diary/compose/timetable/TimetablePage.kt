package io.github.taetae98coding.diary.compose.timetable

import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

internal fun TimetableType.pageOf(date: LocalDate): Int {
    val page =
        when (this) {
            TimetableType.DAY -> MinDate.daysUntil(date)
            TimetableType.WEEK -> MinDate.daysUntil(date.sundayOfWeek()) / DAYS_PER_WEEK
        }

    return page.coerceAtLeast(0)
}

internal fun TimetableType.dateRangeAt(page: Int): LocalDateRange =
    when (this) {
        TimetableType.DAY -> {
            val date = MinDate.plus(page, DateTimeUnit.DAY)

            date..date
        }

        TimetableType.WEEK -> {
            val start = MinDate.plus(page * DAYS_PER_WEEK, DateTimeUnit.DAY)

            start..start.plus(DAYS_PER_WEEK - 1, DateTimeUnit.DAY)
        }
    }

private val MinDate = LocalDate(year = 1, month = 1, day = 1).sundayOfWeek()
