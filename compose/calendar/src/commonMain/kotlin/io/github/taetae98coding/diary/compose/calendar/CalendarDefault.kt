package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

// 밝은 화면에서는 작은 글자의 대비를 유지하고, 어두운 화면에서는 채도를 살린 밝은 Material 계열 강조색.
private val LightSundayAndHolidayColor = Color(color = 0xFFC62828)
private val LightSaturdayColor = Color(color = 0xFF1565C0)
private val DarkSundayAndHolidayColor = Color(color = 0xFFFFCDD2)
private val DarkSaturdayColor = Color(color = 0xFFBBDEFB)

// 날짜 숫자와 아이템을 가리지 않도록 주 색상을 옅게 적용한다.
private const val SELECT_BACKGROUND_ALPHA = 0.24F

public object CalendarDefault {
    // docs/design/styles.md의 `캘린더 아이템 모양`
    public val itemShape: Shape = RoundedCornerShape(4.dp)

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
