package io.github.taetae98coding.diary.work.mcp.tool.json

import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import kotlin.random.Random
import kotlin.random.nextInt

private const val HEX_LENGTH = 6
private const val HEX_RADIX = 16
private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_MASK = 0xFF000000.toInt()

internal fun colorParameter(description: String): McpToolParameter =
    McpToolParameter(
        name = "color",
        type = McpToolParameterType.STRING,
        description = "$description Use a hex color like #4285F4.",
    )

internal fun Long.toColorHex(): String {
    val rgb = toInt() and RGB_MASK

    return "#" + rgb.toString(radix = HEX_RADIX).padStart(length = HEX_LENGTH, padChar = '0').uppercase()
}

internal fun McpToolArguments.colorOrNull(): Long? {
    val text = stringOrNull("color") ?: return null
    val hex = text.removePrefix("#")

    if (hex.length != HEX_LENGTH || hex.any { char -> char.digitToIntOrNull(radix = HEX_RADIX) == null }) {
        throw McpToolArgumentException(message = "'color' must be a hex color like #4285F4.")
    }

    return (ALPHA_MASK or hex.toInt(radix = HEX_RADIX)).toLong()
}

internal fun McpToolArguments.colorOrRandom(random: Random = Random.Default): Long = colorOrNull() ?: (ALPHA_MASK or random.nextInt(range = 0..RGB_MASK)).toLong()
