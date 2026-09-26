package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.CalendarEvent
import io.github.taetae98coding.diary.compose.calendar.calendarClick
import io.github.taetae98coding.diary.compose.calendar.calendarWeekContentDescription
import io.github.taetae98coding.diary.compose.calendar.dateRangeAt
import io.github.taetae98coding.diary.compose.calendar.dayofmonth.CalendarDayOfMonthRow
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.calendar.grid.verticalgrid.CalendarWeekOfMonthLazyVerticalGrid
import io.github.taetae98coding.diary.compose.calendar.scroll.CalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.scroll.rememberCalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.select.CalendarSelectState
import io.github.taetae98coding.diary.compose.calendar.select.calendarSelectBackground
import io.github.taetae98coding.diary.compose.calendar.select.rememberCalendarSelectState
import io.github.taetae98coding.diary.compose.calendar.text.CalendarBarText
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth

@Composable
public fun CalendarWeekOfMonth(
    yearMonth: YearMonth,
    weekOfMonth: Int,
    modifier: Modifier = Modifier,
    selectState: CalendarSelectState = rememberCalendarSelectState(),
    itemScrollState: CalendarItemScrollState = rememberCalendarItemScrollState(),
    onEvent: ((CalendarEvent) -> Unit)? = null,
    isDateClickEnabled: Boolean = false,
    isWeekClickEnabled: Boolean = false,
    holidayProvider: () -> List<LocalDateRange> = { emptyList() },
    primaryDateProvider: () -> List<LocalDate> = { emptyList() },
    colors: CalendarColor = CalendarDefault.colors(),
    content: CalendarWeekOfMonthGridScope.() -> Unit,
) {
    Column(
        modifier =
            modifier.calendarSelectBackground(
                yearMonth = yearMonth,
                weekOfMonth = weekOfMonth,
                state = selectState,
                color = CalendarDefault.selectBackgroundColor(),
            ),
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(CalendarWeekOfMonthDefaults.DividerSpacing))
        CalendarDayOfMonthRow(
            yearMonth = yearMonth,
            weekOfMonth = weekOfMonth,
            modifier = Modifier.fillMaxWidth(),
            onEvent = onEvent?.takeIf { isDateClickEnabled },
            holidayProvider = holidayProvider,
            primaryDateProvider = primaryDateProvider,
            colors = colors,
        )
        CalendarWeekOfMonthLazyVerticalGrid(
            yearMonth = yearMonth,
            weekOfMonth = weekOfMonth,
            modifier =
                Modifier
                    .weight(1F)
                    .then(
                        onEvent?.takeIf { isWeekClickEnabled }?.let {
                            Modifier.calendarWeekClick(
                                yearMonth = yearMonth,
                                weekOfMonth = weekOfMonth,
                                onEvent = it,
                            )
                        } ?: Modifier,
                    ),
            itemScrollState = itemScrollState,
            content = content,
        )
    }
}

@Composable
private fun Modifier.calendarWeekClick(
    yearMonth: YearMonth,
    weekOfMonth: Int,
    onEvent: (CalendarEvent) -> Unit,
): Modifier {
    val dateRange = remember(yearMonth, weekOfMonth) { yearMonth.dateRangeAt(weekOfMonth = weekOfMonth) }
    val contentDescription = calendarWeekContentDescription(startDate = dateRange.start)

    return semantics { this.contentDescription = contentDescription }
        .calendarClick { onEvent(CalendarEvent.ClickWeek(startDate = dateRange.start)) }
}

@ComponentPreview
@Composable
private fun CalendarWeekOfMonthPreview() {
    DiaryTheme {
        Surface {
            CalendarWeekOfMonth(
                yearMonth = YearMonth(year = 2026, month = 7),
                weekOfMonth = 2,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(96.dp),
            ) {
                group {
                    item(dateRange = LocalDate(year = 2026, month = 7, day = 13)..LocalDate(year = 2026, month = 7, day = 15)) {
                        CalendarBarText(text = "메모 제목")
                    }
                }
            }
        }
    }
}
