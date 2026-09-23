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

// 연휴가 걸친 주는 시작일이 속한 주부터 종료일이 속한 주까지이며, 각 주의 기준 달은 그 주의 일요일이 속한 달이다.
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

// 연차 날짜는 평일 색상을 유지하므로 공휴일 날짜만 강조 대상으로 넘긴다.
internal fun GoldenHoliday.holidayDateRangeList(): List<LocalDateRange> = holidayList.map { holiday -> holiday.dateRange }
