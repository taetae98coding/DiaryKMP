@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.dayOfWeekColor
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayBasedNumber
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringArrayResource

@Composable
internal fun TimetableHeader(
    dateRange: LocalDateRange,
    modifier: Modifier = Modifier,
    nowProvider: () -> LocalDateTime? = { null },
    colors: CalendarColor = CalendarDefault.colors(),
) {
    val defaultContentColor = LocalContentColor.current
    val dayOfWeekTitleList = stringArrayResource(Res.array.timetable_day_of_week_titles)

    Row(modifier = modifier) {
        Spacer(modifier = Modifier.width(TimetableDefaults.TimeColumnWidth))
        dateRange.forEach { date ->
            TimetableHeaderDate(
                date = date,
                modifier = Modifier.weight(1F),
                dayOfWeekTitle = dayOfWeekTitleList.getOrNull(date.dayOfWeek.sundayBasedNumber).orEmpty(),
                color = colors.dayOfWeekColor(dayOfWeek = date.dayOfWeek, defaultColor = defaultContentColor),
                nowProvider = nowProvider,
            )
        }
    }
}

@Composable
private fun TimetableHeaderDate(
    date: LocalDate,
    modifier: Modifier = Modifier,
    dayOfWeekTitle: String = "",
    color: Color = LocalContentColor.current,
    nowProvider: () -> LocalDateTime? = { null },
) {
    val currentNowProvider by rememberUpdatedState(nowProvider)
    val isPrimaryDate by remember(date) { derivedStateOf { currentNowProvider()?.date == date } }
    val primaryColor = DiaryTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayOfWeekTitle,
            color = color,
            style = DiaryTheme.typography.labelSmallEmphasized,
        )
        Box(
            modifier =
                Modifier
                    .size(TimetableDefaults.PrimaryDateContainerSize)
                    .styleable {
                        if (isPrimaryDate) {
                            shape(CircleShape)
                            background(primaryColor)
                        }
                    },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.day.toString(),
                color = if (isPrimaryDate) DiaryTheme.colorScheme.onPrimary else color,
                style = DiaryTheme.typography.titleMedium,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun TimetableHeaderPreview() {
    val dateRange = remember { LocalDate(year = 2026, month = 9, day = 20)..LocalDate(year = 2026, month = 9, day = 26) }
    val now = remember { LocalDateTime(year = 2026, month = 9, day = 23, hour = 10, minute = 0) }

    DiaryTheme {
        Surface {
            TimetableHeader(
                dateRange = dateRange,
                modifier = Modifier.fillMaxWidth(),
                nowProvider = { now },
            )
        }
    }
}
