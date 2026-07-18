package io.github.taetae98coding.diary.library.compose.ui.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.random.Random
import kotlin.random.nextInt

private const val HEX_LENGTH = 6
private const val HEX_RADIX = 16
private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_MASK = 0xFF000000.toInt()
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val CHANNEL_MASK = 0xFF

public fun randomColor(random: Random = Random.Default): Color = Color(color = ALPHA_MASK or random.nextInt(range = 0..RGB_MASK))

public fun Color.toHexString(): String {
    val rgb = toArgb() and RGB_MASK
    val hex = rgb.toString(radix = HEX_RADIX).padStart(length = HEX_LENGTH, padChar = '0')

    return "#${hex.uppercase()}"
}

public fun Color.toRgbString(): String {
    val argb = toArgb()
    val red = argb shr RED_SHIFT and CHANNEL_MASK
    val green = argb shr GREEN_SHIFT and CHANNEL_MASK
    val blue = argb and CHANNEL_MASK

    return "RGB($red, $green, $blue)"
}

public fun parseHexColorOrNull(text: CharSequence): Color? {
    val hex = text.removePrefix("#")

    if (hex.length != HEX_LENGTH || hex.any { !it.isHexDigit() }) {
        return null
    }

    val rgb = hex.toString().toInt(radix = HEX_RADIX)

    return Color(color = ALPHA_MASK or rgb)
}

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
