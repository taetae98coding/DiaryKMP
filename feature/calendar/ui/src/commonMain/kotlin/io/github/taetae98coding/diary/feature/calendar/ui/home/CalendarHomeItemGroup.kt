package io.github.taetae98coding.diary.feature.calendar.ui.home

import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.library.kotlinx.datetime.overlaps
import kotlinx.datetime.LocalDateRange

internal fun CalendarWeekOfMonthGridScope.isSingleRow(
    weatherProvider: () -> List<CalendarWeather>,
    holidayProvider: () -> List<Holiday>,
    memoProvider: () -> List<CalendarMemo>,
    birthdayProvider: () -> List<CalendarContactBirthday>,
): Boolean {
    val weatherDateList = weatherProvider().map { weather -> weather.date }.filter { date -> date in dateRange }
    val itemDateRangeList =
        (
            memoProvider().map { memo -> memo.dateTime.toDateRange() } +
                holidayProvider().map { holiday -> holiday.dateRange } +
                birthdayProvider().map { birthday -> birthday.toDateRange() }
        ).filter { itemDateRange -> itemDateRange overlaps dateRange }
            .map { itemDateRange -> itemDateRange.start.coerceIn(dateRange)..itemDateRange.endInclusive.coerceIn(dateRange) }

    return weatherDateList.isNotEmpty() &&
        weatherDateList.none { date -> itemDateRangeList.any { itemDateRange -> date in itemDateRange } } &&
        !itemDateRangeList.hasOverlappedDateRange()
}

private fun List<LocalDateRange>.hasOverlappedDateRange(): Boolean =
    sortedBy { dateRange -> dateRange.start }
        .zipWithNext()
        .any { (previous, next) -> next.start <= previous.endInclusive }
