package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.foundation.text.input.TextFieldState
import kotlin.math.round

internal fun Double.toCoordinateText(): String {
    val scaled = round(this * COORDINATE_SCALE) / COORDINATE_SCALE

    return scaled.toString()
}

internal fun TextFieldState.decimalOrNaN(): Double =
    text
        .toString()
        .trim()
        .toDoubleOrNull()
        ?: Double.NaN

private const val COORDINATE_SCALE = 1_000_000.0
