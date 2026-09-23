package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public object ColorPickerDialogDefaults {
    public val PreviewHeight: Dp = 140.dp

    public val RandomButtonPadding: Dp = 8.dp

    public val SliderHorizontalPadding: Dp = 24.dp

    public val SliderVerticalPadding: Dp = 8.dp

    public val SliderSpacing: Dp = 8.dp

    public val ChannelSliderSpacing: Dp = 8.dp

    public val ButtonHorizontalPadding: Dp = 24.dp

    public val ButtonBottomPadding: Dp = 16.dp

    public val ButtonSpacing: Dp = 8.dp

    private val LightRedLabelColor = Color(color = 0xFFB3261E)
    private val LightGreenLabelColor = Color(color = 0xFF386A20)
    private val LightBlueLabelColor = Color(color = 0xFF0B57D0)
    private val DarkRedLabelColor = Color(color = 0xFFF2B8B5)
    private val DarkGreenLabelColor = Color(color = 0xFF9CD67D)
    private val DarkBlueLabelColor = Color(color = 0xFFA8C7FA)

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
