package io.github.taetae98coding.diary.library.compose.ui.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

public fun Color.toColorLong(): Long = toArgb().toLong()

public fun Long.toColor(): Color = Color(color = toInt())
