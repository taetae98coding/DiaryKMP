package io.github.taetae98coding.diary.compose.calendar.dayofmonth

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.CalendarEvent
import io.github.taetae98coding.diary.compose.calendar.calendarClick
import io.github.taetae98coding.diary.compose.calendar.calendarDateContentDescription
import io.github.taetae98coding.diary.compose.calendar.dateAt
import io.github.taetae98coding.diary.compose.calendar.dayOfWeekColor
import io.github.taetae98coding.diary.compose.calendar.week.CalendarWeekOfMonthDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

@Composable
internal fun CalendarDayOfMonth(
    dayOfWeek: DayOfWeek,
    yearMonth: YearMonth,
    weekOfMonth: Int,
    modifier: Modifier = Modifier,
    onEvent: ((CalendarEvent) -> Unit)? = null,
    holidayProvider: () -> List<LocalDateRange> = { emptyList() },
    primaryDateProvider: () -> List<LocalDate> = { emptyList() },
    colors: CalendarColor = CalendarDefault.colors(),
) {
    val date =
        remember(dayOfWeek, yearMonth, weekOfMonth) {
            yearMonth.dateAt(weekOfMonth = weekOfMonth, dayOfWeek = dayOfWeek)
        }
    val currentHolidayProvider by rememberUpdatedState(holidayProvider)
    val currentPrimaryDateProvider by rememberUpdatedState(primaryDateProvider)
    val isHoliday by
        remember(date) {
            derivedStateOf {
                currentHolidayProvider().any { range -> date in range }
            }
        }
    val isPrimaryDate by
        remember(date) {
            derivedStateOf {
                currentPrimaryDateProvider().any { primaryDate -> primaryDate == date }
            }
        }
    val containerColor =
        if (isPrimaryDate) {
            DiaryTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    val contentColor =
        if (isPrimaryDate) {
            DiaryTheme.colorScheme.onPrimary
        } else {
            colors.dayOfWeekColor(
                dayOfWeek = date.dayOfWeek,
                defaultColor = LocalContentColor.current,
                isHoliday = isHoliday,
            )
        }
    val isAdjacentMonth = !isPrimaryDate && date.yearMonth != yearMonth

    CalendarDayOfMonthText(
        day = date.day,
        containerColor = containerColor.adjust(isAdjacentMonth = isAdjacentMonth),
        contentColor = contentColor.adjust(isAdjacentMonth = isAdjacentMonth),
        modifier =
            modifier.then(
                onEvent?.let {
                    val contentDescription = calendarDateContentDescription(date = date)

                    Modifier
                        .semantics { this.contentDescription = contentDescription }
                        .calendarClick { it(CalendarEvent.ClickDate(date = date)) }
                } ?: Modifier,
            ),
    )
}

private fun Color.adjust(isAdjacentMonth: Boolean): Color =
    if (isAdjacentMonth) {
        copy(alpha = alpha * CalendarWeekOfMonthDefaults.ADJACENT_MONTH_DAY_ALPHA)
    } else {
        this
    }

@ComponentPreview
@Composable
private fun CalendarDayOfMonthPreview() {
    DiaryTheme {
        Surface {
            CalendarDayOfMonth(
                dayOfWeek = DayOfWeek.MONDAY,
                yearMonth = YearMonth(year = 2026, month = 7),
                weekOfMonth = 2,
            )
        }
    }
}
