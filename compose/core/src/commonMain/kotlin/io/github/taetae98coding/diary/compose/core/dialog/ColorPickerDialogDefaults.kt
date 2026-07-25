package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// M3 baseline 팔레트의 tone 40(라이트)과 tone 80(다크) 계열로 맞춘 채널 라벨 컬러.
private val LightRedLabelColor = Color(color = 0xFFB3261E)
private val LightGreenLabelColor = Color(color = 0xFF386A20)
private val LightBlueLabelColor = Color(color = 0xFF0B57D0)
private val DarkRedLabelColor = Color(color = 0xFFF2B8B5)
private val DarkGreenLabelColor = Color(color = 0xFF9CD67D)
private val DarkBlueLabelColor = Color(color = 0xFFA8C7FA)

public object ColorPickerDialogDefaults {
    @Composable
    public fun colors(darkTheme: Boolean = isSystemInDarkTheme()): ColorPickerDialogColors =
        if (darkTheme) {
            ColorPickerDialogColors(
                redLabelColor = DarkRedLabelColor,
                greenLabelColor = DarkGreenLabelColor,
                blueLabelColor = DarkBlueLabelColor,
            )
        } else {
            ColorPickerDialogColors(
                redLabelColor = LightRedLabelColor,
                greenLabelColor = LightGreenLabelColor,
                blueLabelColor = LightBlueLabelColor,
            )
        }
}
