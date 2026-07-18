package io.github.taetae98coding.diary.library.compose.ui.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private const val LUMINANCE_THRESHOLD = 0.5F

public fun Color.contentColor(): Color = if (luminance() < LUMINANCE_THRESHOLD) Color.White else Color.Black
