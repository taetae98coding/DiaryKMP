package io.github.taetae98coding.diary.compose.calendar.month

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.WEEKS_PER_MONTH
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.calendar.scroll.CalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.scroll.rememberCalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.select.CalendarSelectState
import io.github.taetae98coding.diary.compose.calendar.select.rememberCalendarSelectState
import io.github.taetae98coding.diary.compose.calendar.text.CalendarBarText
import io.github.taetae98coding.diary.compose.calendar.week.CalendarWeekOfMonth
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth

@Composable
internal fun CalendarMonth(
    yearMonth: YearMonth,
    modifier: Modifier = Modifier,
    selectState: CalendarSelectState = rememberCalendarSelectState(),
    itemScrollState: CalendarItemScrollState = rememberCalendarItemScrollState(),
    holidayProvider: () -> List<LocalDateRange> = { emptyList() },
    primaryDateProvider: () -> List<LocalDate> = { emptyList() },
    colors: CalendarColor = CalendarDefault.colors(),
    content: CalendarWeekOfMonthGridScope.() -> Unit,
) {
    Column(modifier = modifier) {
        repeat(WEEKS_PER_MONTH) { weekOfMonth ->
            CalendarWeekOfMonth(
                yearMonth = yearMonth,
                weekOfMonth = weekOfMonth,
                modifier = Modifier.weight(1F),
                selectState = selectState,
                itemScrollState = itemScrollState,
                holidayProvider = holidayProvider,
                primaryDateProvider = primaryDateProvider,
                colors = colors,
                content = content,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun CalendarMonthPreview() {
    DiaryTheme {
        Surface {
            CalendarMonth(
                yearMonth = YearMonth(year = 2026, month = 7),
                modifier = Modifier.fillMaxSize(),
            ) {
                group {
                    item(dateRange = LocalDate(year = 2026, month = 7, day = 6)..LocalDate(year = 2026, month = 7, day = 8)) {
                        CalendarBarText(text = "메모 제목")
                    }
                }
            }
        }
    }
}
