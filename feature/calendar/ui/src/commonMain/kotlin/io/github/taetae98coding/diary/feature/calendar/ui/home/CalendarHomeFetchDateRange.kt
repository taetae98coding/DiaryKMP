package io.github.taetae98coding.diary.feature.calendar.ui.home

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

internal fun YearMonth.calendarHomeFetchDateRange(): LocalDateRange = minus(2, DateTimeUnit.MONTH).firstDay..plus(2, DateTimeUnit.MONTH).lastDay
