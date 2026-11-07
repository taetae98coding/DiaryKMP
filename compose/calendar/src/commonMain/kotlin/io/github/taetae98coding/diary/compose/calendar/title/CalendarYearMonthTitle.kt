package io.github.taetae98coding.diary.compose.calendar.title

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.calendar.Res
import io.github.taetae98coding.diary.compose.calendar.calendar_home_month_titles
import io.github.taetae98coding.diary.compose.calendar.calendar_home_year_month_title
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
public fun calendarYearMonthTitle(yearMonth: YearMonth): String {
    val monthTitle = stringArrayResource(Res.array.calendar_home_month_titles).getOrNull(yearMonth.month.number - 1).orEmpty()

    return stringResource(Res.string.calendar_home_year_month_title, yearMonth.year.toString(), monthTitle)
}
