package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DayOfWeek

@Immutable
public data class CalendarColor(
    val sundayAndHolidayColor: Color,
    val saturdayColor: Color,
)

public fun CalendarColor.dayOfWeekColor(
    dayOfWeek: DayOfWeek,
    defaultColor: Color,
    isHoliday: Boolean = false,
): Color =
    when {
        isHoliday || dayOfWeek == DayOfWeek.SUNDAY -> sundayAndHolidayColor
        dayOfWeek == DayOfWeek.SATURDAY -> saturdayColor
        else -> defaultColor
    }
