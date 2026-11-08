package io.github.taetae98coding.diary.work.appaction.place

import kotlin.math.abs
import kotlin.math.roundToLong

private const val FRACTION_DIGITS = 9
private const val FRACTION_SCALE = 1_000_000_000L

// 좌표 글은 지수 표기(1.0E-5)를 받지 않으므로, iOS가 숫자로 받은 좌표를 소수 표기 글로 바꿔 데스크톱 연결과 같은 입력으로 넘긴다.
// 범위를 크게 벗어난 값은 그대로 글로 바꿔도 앱 기능이 올바르지 않은 좌표로 실패시킨다.
public fun appActionCoordinateText(value: Double): String {
    if (!value.isFinite() || abs(value) >= FRACTION_SCALE) return value.toString()

    val scaled = (abs(value) * FRACTION_SCALE).roundToLong()
    val sign = if (value < 0 && scaled != 0L) "-" else ""
    val fraction = (scaled % FRACTION_SCALE).toString().padStart(length = FRACTION_DIGITS, padChar = '0').trimEnd('0')
    val integer = scaled / FRACTION_SCALE

    return if (fraction.isEmpty()) "$sign$integer" else "$sign$integer.$fraction"
}
