package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.foundation.text.input.TextFieldState
import io.github.taetae98coding.diary.domain.place.PLACE_COORDINATE_FRACTION_DIGITS
import io.github.taetae98coding.diary.domain.place.PLACE_COORDINATE_SCALE
import kotlin.math.absoluteValue
import kotlin.math.round

// Double.toString은 끝자리 0을 지우고 작은 값을 지수 표기로 바꾸며 그 방식이 플랫폼마다 달라서, 정수로 옮겨 자리를 직접 채운다.
internal fun Double.toCoordinateText(): String {
    val scaled = round(this * PLACE_COORDINATE_SCALE).toLong()
    val sign = if (scaled < 0) "-" else ""
    val absolute = scaled.absoluteValue
    val integerPart = absolute / PLACE_COORDINATE_SCALE
    val fractionPart =
        (absolute % PLACE_COORDINATE_SCALE)
            .toString()
            .padStart(length = PLACE_COORDINATE_FRACTION_DIGITS, padChar = '0')

    return "$sign$integerPart.$fractionPart"
}

// toDoubleOrNull은 지수·16진 표기와 f·d 접미사, NaN·Infinity도 숫자로 읽으므로 형식을 먼저 걸러낸다.
internal fun TextFieldState.decimalOrNaN(): Double =
    text
        .toString()
        .trim()
        .takeIf { value -> SIGNED_DECIMAL_REGEX.matches(value) }
        ?.toDoubleOrNull()
        ?: Double.NaN

private val SIGNED_DECIMAL_REGEX = Regex("[+-]?[0-9]+(\\.[0-9]+)?")
