package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

private val LightSundayAndHolidayColor = Color(color = 0xFFC62828)
private val LightSaturdayColor = Color(color = 0xFF1565C0)
private val DarkSundayAndHolidayColor = Color(color = 0xFFFFCDD2)
private val DarkSaturdayColor = Color(color = 0xFFBBDEFB)

public object CalendarDefault {
    public val itemShape: Shape = RoundedCornerShape(4.dp)
    public const val SELECT_BACKGROUND_ALPHA: Float = 0.24F

    @Composable
    public fun colors(darkTheme: Boolean = isSystemInDarkTheme()): CalendarColor =
        if (darkTheme) {
            CalendarColor(
                sundayAndHolidayColor = DarkSundayAndHolidayColor,
                saturdayColor = DarkSaturdayColor,
            )
        } else {
            CalendarColor(
                sundayAndHolidayColor = LightSundayAndHolidayColor,
                saturdayColor = LightSaturdayColor,
            )
        }

    @Composable
    public fun selectBackgroundColor(): Color = DiaryTheme.colorScheme.primary.copy(alpha = SELECT_BACKGROUND_ALPHA)
}
