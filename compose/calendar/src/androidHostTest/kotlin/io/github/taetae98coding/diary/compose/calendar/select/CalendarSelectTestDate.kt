package io.github.taetae98coding.diary.compose.calendar.select

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

internal val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
internal val JUNE_2026 = YearMonth(year = 2026, month = Month.JUNE)
internal val AUGUST_2026 = YearMonth(year = 2026, month = Month.AUGUST)
internal val SEPTEMBER_2026 = YearMonth(year = 2026, month = Month.SEPTEMBER)
internal val FIRST_YEAR_MONTH = YearMonth(year = 1, month = Month.JANUARY)

internal fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

internal fun june(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JUNE, day = day)

internal fun august(day: Int): LocalDate = LocalDate(year = 2026, month = Month.AUGUST, day = day)

internal fun september(day: Int): LocalDate = LocalDate(year = 2026, month = Month.SEPTEMBER, day = day)

internal fun firstYearJanuary(day: Int): LocalDate = LocalDate(year = 1, month = Month.JANUARY, day = day)
