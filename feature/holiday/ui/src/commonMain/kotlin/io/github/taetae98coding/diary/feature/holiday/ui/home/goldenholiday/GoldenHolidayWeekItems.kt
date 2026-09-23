package io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import kotlinx.datetime.LocalDateRange

private const val ANNUAL_LEAVE_KEY_PREFIX = "annual-leave-"

internal fun CalendarWeekOfMonthGridGroupScope.holidayItems(
    holidayList: List<Holiday>,
    color: Color,
) {
    holidayList.forEach { holiday ->
        item(
            dateRange = holiday.dateRange,
            key = holiday.toString(),
        ) {
            CalendarText(
                text = holiday.name,
                modifier = Modifier.animateItem(),
                color = color,
            )
        }
    }
}

internal fun CalendarWeekOfMonthGridGroupScope.annualLeaveItems(
    dateRangeList: List<LocalDateRange>,
    label: String,
    color: Color,
) {
    dateRangeList.forEach { dateRange ->
        item(
            dateRange = dateRange,
            key = "$ANNUAL_LEAVE_KEY_PREFIX$dateRange",
        ) {
            CalendarText(
                text = label,
                modifier = Modifier.animateItem(),
                color = color,
            )
        }
    }
}
