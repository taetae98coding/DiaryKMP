package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.calendar.dayofweek.CalendarDayOfWeekRow
import io.github.taetae98coding.diary.compose.calendar.drag.calendarDrag
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.calendar.month.CalendarMonth
import io.github.taetae98coding.diary.compose.calendar.text.CalendarBarText
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

@Composable
public fun Calendar(
    modifier: Modifier = Modifier,
    state: CalendarState = rememberCalendarState(),
    onSelect: ((LocalDateRange) -> Unit)? = null,
    holidayProvider: () -> List<LocalDateRange> = { emptyList() },
    primaryDateProvider: () -> List<LocalDate> = { emptyList() },
    colors: CalendarColor = CalendarDefault.colors(),
    content: CalendarWeekOfMonthGridScope.() -> Unit,
) {
    Column(modifier = modifier) {
        CalendarDayOfWeekRow(
            modifier = Modifier.fillMaxWidth(),
            colors = colors,
        )
        HorizontalPager(
            state = state.pagerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
                    .then(onSelect?.let { Modifier.calendarDrag(state = state, onSelect = it) } ?: Modifier),
        ) { page ->
            CalendarMonth(
                yearMonth = page.toYearMonth(),
                selectState = state.selectState,
                itemScrollState = state.itemScrollState,
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
private fun CalendarPreview() {
    DiaryTheme {
        Surface {
            Calendar(modifier = Modifier.fillMaxSize()) {
                group {
                    item(dateRange = LocalDate(year = 2026, month = 7, day = 6)..LocalDate(year = 2026, month = 7, day = 8)) {
                        CalendarBarText(text = "메모 제목")
                    }
                }
            }
        }
    }
}
