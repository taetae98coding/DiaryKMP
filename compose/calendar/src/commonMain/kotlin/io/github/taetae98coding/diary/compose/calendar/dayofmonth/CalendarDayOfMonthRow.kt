package io.github.taetae98coding.diary.compose.calendar.dayofmonth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.CalendarEvent
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayBasedDayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth

@Composable
internal fun CalendarDayOfMonthRow(
    yearMonth: YearMonth,
    weekOfMonth: Int,
    modifier: Modifier = Modifier,
    onEvent: ((CalendarEvent) -> Unit)? = null,
    holidayProvider: () -> List<LocalDateRange> = { emptyList() },
    primaryDateProvider: () -> List<LocalDate> = { emptyList() },
    colors: CalendarColor = CalendarDefault.colors(),
) {
    Row(modifier = modifier) {
        repeat(DAYS_PER_WEEK) { index ->
            CalendarDayOfMonth(
                dayOfWeek = sundayBasedDayOfWeek(number = index),
                yearMonth = yearMonth,
                weekOfMonth = weekOfMonth,
                modifier = Modifier.weight(1F),
                onEvent = onEvent,
                holidayProvider = holidayProvider,
                primaryDateProvider = primaryDateProvider,
                colors = colors,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun CalendarDayOfMonthRowPreview() {
    DiaryTheme {
        Surface {
            CalendarDayOfMonthRow(
                yearMonth = YearMonth(year = 2026, month = 7),
                weekOfMonth = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
