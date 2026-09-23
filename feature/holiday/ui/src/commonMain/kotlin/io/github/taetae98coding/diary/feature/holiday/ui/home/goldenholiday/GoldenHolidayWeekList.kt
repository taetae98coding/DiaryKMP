package io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.CalendarColor
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.select.calendarWeekSelectDrag
import io.github.taetae98coding.diary.compose.calendar.select.rememberCalendarSelectState
import io.github.taetae98coding.diary.compose.calendar.week.CalendarWeekOfMonth
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_annual_leave_item_label
import io.github.taetae98coding.diary.feature.holiday.ui.previewGoldenHoliday
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayOfWeek
import kotlinx.datetime.LocalDateRange
import org.jetbrains.compose.resources.stringResource

private val WeekHeight = 96.dp

@Composable
internal fun GoldenHolidayWeekList(
    goldenHoliday: GoldenHoliday,
    onSelectDate: (LocalDateRange) -> Unit,
    modifier: Modifier = Modifier,
    colors: CalendarColor = CalendarDefault.colors(),
) {
    val annualLeaveLabel = stringResource(Res.string.holiday_annual_leave_item_label)
    val annualLeaveColor = DiaryTheme.colorScheme.primary
    val selectState = rememberCalendarSelectState()
    val calendarWeekList = goldenHoliday.calendarWeekList()

    Column(
        modifier =
            modifier.calendarWeekSelectDrag(
                state = selectState,
                firstSunday = goldenHoliday.dateRange.start.sundayOfWeek(),
                weekCount = calendarWeekList.size,
                onSelect = { dateRange ->
                    selectState.clear()
                    onSelectDate(dateRange)
                },
            ),
    ) {
        calendarWeekList.forEach { calendarWeek ->
            CalendarWeekOfMonth(
                yearMonth = calendarWeek.yearMonth,
                weekOfMonth = calendarWeek.weekOfMonth,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(WeekHeight),
                selectState = selectState,
                holidayProvider = { goldenHoliday.holidayDateRangeList() },
                colors = colors,
            ) {
                group {
                    holidayItems(
                        holidayList = goldenHoliday.holidayList,
                        color = colors.sundayAndHolidayColor,
                    )
                    annualLeaveItems(
                        dateRangeList = goldenHoliday.annualLeaveDateRangeList,
                        label = annualLeaveLabel,
                        color = annualLeaveColor,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun GoldenHolidayWeekListPreview() {
    val goldenHoliday = remember { previewGoldenHoliday() }

    DiaryTheme {
        Surface {
            GoldenHolidayWeekList(
                goldenHoliday = goldenHoliday,
                onSelectDate = {},
            )
        }
    }
}
