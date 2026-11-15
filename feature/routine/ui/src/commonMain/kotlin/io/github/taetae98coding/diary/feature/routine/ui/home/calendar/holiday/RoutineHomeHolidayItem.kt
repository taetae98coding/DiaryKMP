package io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.text.CalendarText
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.feature.routine.ui.home.press.longPress

internal fun CalendarWeekOfMonthGridGroupScope.holidayItem(
    holidayProvider: () -> List<Holiday>,
    holidayNameColor: Color,
    nonHolidayNameColor: Color,
    onHolidayClick: (Holiday) -> Unit,
) {
    holidayProvider().forEach { holiday ->
        item(
            dateRange = holiday.dateRange,
            key = holiday.toString(),
        ) {
            CalendarText(
                text = holiday.name,
                modifier =
                    Modifier
                        .animateItem()
                        .clip(CalendarDefaults.itemShape)
                        .longPress { }
                        .clickable(role = Role.Button) { onHolidayClick(holiday) },
                color = if (holiday.isHoliday) holidayNameColor else nonHolidayNameColor,
            )
        }
    }
}
