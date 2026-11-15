package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private const val FETCH_MARGIN_MONTH_COUNT = 2

internal fun YearMonth.routineHomeFetchDateRange(): LocalDateRange = minus(FETCH_MARGIN_MONTH_COUNT, DateTimeUnit.MONTH).firstDay..plus(FETCH_MARGIN_MONTH_COUNT, DateTimeUnit.MONTH).lastDay
