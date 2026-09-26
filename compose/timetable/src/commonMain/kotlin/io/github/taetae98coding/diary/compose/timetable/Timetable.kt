package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Composable
public fun Timetable(
    modifier: Modifier = Modifier,
    state: TimetableState = rememberTimetableState(),
    nowProvider: () -> LocalDateTime? = { null },
    colors: CalendarColor = CalendarDefault.colors(),
    content: TimetableScope.() -> Unit,
) {
    HorizontalPager(
        state = state.pagerState,
        modifier = modifier,
    ) { page ->
        TimetablePage(
            page = page,
            modifier = Modifier.fillMaxSize(),
            state = state,
            nowProvider = nowProvider,
            colors = colors,
            content = content,
        )
    }
}

@Composable
private fun TimetablePage(
    page: Int,
    modifier: Modifier = Modifier,
    state: TimetableState = rememberTimetableState(),
    nowProvider: () -> LocalDateTime? = { null },
    colors: CalendarColor = CalendarDefault.colors(),
    content: TimetableScope.() -> Unit,
) {
    val dateRange = remember(state, page) { state.dateRangeAt(page) }
    val scope = TimetableScopeImpl().apply(content)
    val allDayItemList = scope.allDayItemList.placeIn(dateRange = dateRange)

    Column(modifier = modifier) {
        TimetableHeader(
            dateRange = dateRange,
            modifier = Modifier.fillMaxWidth(),
            nowProvider = nowProvider,
            colors = colors,
        )
        HorizontalDivider()
        if (allDayItemList.isNotEmpty()) {
            TimetableAllDay(
                dateRange = dateRange,
                modifier = Modifier.fillMaxWidth(),
                itemList = allDayItemList,
            )
            HorizontalDivider()
        }
        TimetableGrid(
            page = page,
            dateRange = dateRange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
            state = state,
            nowProvider = nowProvider,
            timeItemList = scope.timeItemList,
        )
    }
}

@ScreenPreview
@Composable
private fun TimetablePreview() {
    val date = remember { LocalDate(year = 2026, month = 7, day = 19) }

    DiaryTheme {
        Surface {
            Timetable(
                modifier = Modifier.fillMaxSize(),
                state = rememberTimetableState(type = TimetableType.WEEK, initialDate = date),
                nowProvider = { LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 30)) },
            ) {
                allDayItem(dateRange = date..date, key = "휴가") { Text(text = "휴가") }
                timeItem(date = date, startTime = LocalTime(hour = 9, minute = 0), endTime = LocalTime(hour = 10, minute = 30), key = "회의") {
                    Text(text = "회의")
                }
            }
        }
    }
}
