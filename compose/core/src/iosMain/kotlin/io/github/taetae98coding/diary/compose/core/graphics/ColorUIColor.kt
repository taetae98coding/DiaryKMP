package io.github.taetae98coding.diary.compose.core.graphics

import androidx.compose.ui.graphics.Color
import platform.UIKit.UIColor

public fun Color.toUIColor(): UIColor =
    UIColor(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble(),
    )
