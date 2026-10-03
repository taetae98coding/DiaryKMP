package io.github.taetae98coding.diary.library.compose.ui.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private const val WCAG_CONTRAST_LUMINANCE_OFFSET = 0.05F

public fun Color.contentColor(): Color = if (contrastRatio(Color.White) > contrastRatio(Color.Black)) Color.White else Color.Black

private fun Color.contrastRatio(other: Color): Float {
    val lighter = maxOf(luminance(), other.luminance())
    val darker = minOf(luminance(), other.luminance())

    return (lighter + WCAG_CONTRAST_LUMINANCE_OFFSET) / (darker + WCAG_CONTRAST_LUMINANCE_OFFSET)
}
