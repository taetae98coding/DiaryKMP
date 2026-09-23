package io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday

import io.github.taetae98coding.diary.compose.calendar.weekOfMonthAt
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

// 담은 공휴일이 다른 항목이 같은 날 시작할 수 있으므로 모든 대안의 기간을 합쳐 식별한다.
internal fun GoldenHolidayGroup.key(): String = optionList.joinToString(separator = "|") { option -> option.dateRange.toString() }

internal fun GoldenHoliday.calendarWeekList(): List<CalendarWeek> {
    val lastSunday = dateRange.endInclusive.sundayOfWeek()

    return generateSequence(dateRange.start.sundayOfWeek()) { sunday -> sunday.plus(DAYS_PER_WEEK, DateTimeUnit.DAY) }
        .takeWhile { sunday -> sunday <= lastSunday }
        .map { sunday ->
            CalendarWeek(
                yearMonth = sunday.yearMonth,
                weekOfMonth = sunday.yearMonth.weekOfMonthAt(sunday),
            )
        }.toList()
}

internal fun GoldenHoliday.holidayDateRangeList(): List<LocalDateRange> = holidayList.map { holiday -> holiday.dateRange }
