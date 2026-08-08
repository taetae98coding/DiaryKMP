package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.material3.Text
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal const val FIRST_ITEM_TEXT: String = "FirstCalendarItem"
internal const val SECOND_ITEM_TEXT: String = "SecondCalendarItem"
internal const val THIRD_ITEM_TEXT: String = "ThirdCalendarItem"
internal const val FOURTH_ITEM_TEXT: String = "FourthCalendarItem"

private const val CALENDAR_TEST_YEAR = 2026

internal fun june(day: Int): LocalDate = LocalDate(year = CALENDAR_TEST_YEAR, month = Month.JUNE, day = day)

internal fun july(day: Int): LocalDate = LocalDate(year = CALENDAR_TEST_YEAR, month = Month.JULY, day = day)

internal fun august(day: Int): LocalDate = LocalDate(year = CALENDAR_TEST_YEAR, month = Month.AUGUST, day = day)

internal fun CalendarWeekOfMonthGridGroupScope.textItem(
    text: String,
    start: LocalDate,
    endInclusive: LocalDate,
) {
    item(
        dateRange = start..endInclusive,
        key = text,
    ) {
        Text(text = text)
    }
}
