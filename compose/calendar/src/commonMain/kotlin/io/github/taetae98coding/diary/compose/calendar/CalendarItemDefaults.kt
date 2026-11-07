package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public object CalendarItemDefaults {
    private val LightNonHolidayNameColor = Color(color = 0xFFBDBDBD)
    private val DarkNonHolidayNameColor = Color(color = 0xFFE0E0E0)

    @Composable
    public fun nonHolidayNameColor(darkTheme: Boolean = isSystemInDarkTheme()): Color =
        if (darkTheme) {
            DarkNonHolidayNameColor
        } else {
            LightNonHolidayNameColor
        }

    @Composable
    public fun birthdayColor(): Color = DiaryTheme.colorScheme.tertiaryContainer
}
