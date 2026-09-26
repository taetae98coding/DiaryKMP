package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun calendarDateContentDescription(date: LocalDate): String =
    stringResource(
        Res.string.calendar_date_content_description,
        calendarMonthTitle(date = date),
        date.day,
    )

@Composable
internal fun calendarWeekContentDescription(startDate: LocalDate): String =
    stringResource(
        Res.string.calendar_week_content_description,
        calendarMonthTitle(date = startDate),
        startDate.day,
    )

@Composable
private fun calendarMonthTitle(date: LocalDate): String =
    stringArrayResource(Res.array.calendar_month_titles)
        .getOrNull(date.month.number - 1)
        .orEmpty()
