package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.foundation.text.input.TextFieldState
import kotlin.math.absoluteValue
import kotlin.math.round

// Double.toString은 끝자리 0을 지우고 작은 값을 지수 표기로 바꾸며 그 방식이 플랫폼마다 달라서, 정수로 옮겨 자리를 직접 채운다.
internal fun Double.toCoordinateText(): String {
    val scaled = round(this * COORDINATE_SCALE_LONG).toLong()
    val sign = if (scaled < 0) "-" else ""
    val absolute = scaled.absoluteValue
    val integerPart = absolute / COORDINATE_SCALE_LONG
    val fractionPart =
        (absolute % COORDINATE_SCALE_LONG)
            .toString()
            .padStart(length = COORDINATE_FRACTION_DIGITS, padChar = '0')

    return "$sign$integerPart.$fractionPart"
}

internal fun TextFieldState.decimalOrNaN(): Double =
    text
        .toString()
        .trim()
        .toDoubleOrNull()
        ?: Double.NaN

private const val COORDINATE_FRACTION_DIGITS = 6
private const val COORDINATE_SCALE_LONG = 1_000_000L
