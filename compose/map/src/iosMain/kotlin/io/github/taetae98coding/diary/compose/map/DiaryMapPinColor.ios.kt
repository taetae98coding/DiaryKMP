package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.graphics.Color
import platform.UIKit.UIColor

internal fun Color.toUIColor(): UIColor =
    UIColor(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble(),
    )
