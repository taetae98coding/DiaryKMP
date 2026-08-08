package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
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
        Spacer(modifier = Modifier.height(2.dp))
        CalendarDayOfMonthRow(
            yearMonth = yearMonth,
            weekOfMonth = weekOfMonth,
            modifier = Modifier.fillMaxWidth(),
            holidayProvider = holidayProvider,
            primaryDateProvider = primaryDateProvider,
            colors = colors,
        )
        CalendarWeekOfMonthLazyVerticalGrid(
            yearMonth = yearMonth,
            weekOfMonth = weekOfMonth,
            modifier = Modifier.weight(1F),
            itemScrollState = itemScrollState,
            content = content,
        )
    }
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
