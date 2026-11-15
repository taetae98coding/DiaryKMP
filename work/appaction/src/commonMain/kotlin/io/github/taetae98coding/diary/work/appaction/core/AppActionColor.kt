package io.github.taetae98coding.diary.work.appaction.core

import org.koin.core.annotation.Factory
import kotlin.random.Random
import kotlin.random.nextInt

private const val HEX_LENGTH = 6
private const val HEX_RADIX = 16
private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_MASK = 0xFF000000.toInt()

internal fun Long.toColorHex(): String {
    val rgb = toInt() and RGB_MASK

    return "#" + rgb.toString(radix = HEX_RADIX).padStart(length = HEX_LENGTH, padChar = '0').uppercase()
}

internal fun String?.toColorArgumentOrNull(): Long? {
    val hex = this?.removePrefix("#") ?: return null

    if (hex.length != HEX_LENGTH || hex.any { char -> char.digitToIntOrNull(radix = HEX_RADIX) == null }) {
        invalidArgument(message = "'color' must be a hex color like #4285F4.")
    }

    return (ALPHA_MASK or hex.toInt(radix = HEX_RADIX)).toLong()
}

internal fun String?.toColorArgumentOrRandom(random: Random = Random.Default): Long = toColorArgumentOrNull() ?: randomColor(random = random)

// 데스크톱 MCP 연결도 컬러를 받지 않는 추가의 컬러를 이 값으로 정한다.
@Factory
public class AppActionRandomColor {
    public fun next(): Long = randomColor(random = Random.Default)
}

private fun randomColor(random: Random): Long = (ALPHA_MASK or random.nextInt(range = 0..RGB_MASK)).toLong()
