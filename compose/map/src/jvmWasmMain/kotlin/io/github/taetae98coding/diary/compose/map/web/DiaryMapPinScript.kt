package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.DiaryMapPinMarkerDefaults
import io.github.taetae98coding.diary.compose.map.PIN_MARKER_PATH_DATA
import io.github.taetae98coding.diary.compose.map.PIN_MARKER_VIEWPORT_SIZE
import io.github.taetae98coding.diary.compose.map.isFinite
import io.github.taetae98coding.diary.compose.map.provider.label

internal fun pinMarkerToScriptValue(): String =
    buildString {
        append("""{ "path": "$PIN_MARKER_PATH_DATA",""")
        append(""" "viewport": $PIN_MARKER_VIEWPORT_SIZE,""")
        append(""" "size": ${DiaryMapPinMarkerDefaults.SIZE_DP},""")
        append(""" "labelHeight": ${DiaryMapPinMarkerDefaults.LABEL_HEIGHT_DP},""")
        append(""" "labelFontSize": ${DiaryMapPinMarkerDefaults.LABEL_FONT_SIZE_SP},""")
        append(""" "labelHorizontalPadding": ${DiaryMapPinMarkerDefaults.LABEL_HORIZONTAL_PADDING_DP},""")
        append(""" "labelCornerRadius": ${DiaryMapPinMarkerDefaults.LABEL_CORNER_RADIUS_DP},""")
        append(""" "labelBackgroundAlpha": ${DiaryMapPinMarkerDefaults.LABEL_BACKGROUND_ALPHA} }""")
    }

internal fun List<DiaryMapPin>.toScriptValue(): String =
    filter { pin -> pin.isFinite }
        .joinToString(prefix = "[", postfix = "]") { pin ->
            buildString {
                append("""{ "id": "${pin.id}",""")
                append(""" "latitude": ${pin.coordinate.latitude},""")
                append(""" "longitude": ${pin.coordinate.longitude},""")
                append(""" "color": "${pin.color.toHexScriptValue()}",""")
                append(""" "label": "${pin.label.toEscapedScriptValue()}" }""")
            }
        }

private fun Color.toHexScriptValue(): String {
    val rgb = toArgb() and RGB_MASK

    return "#" + rgb.toString(HEX_RADIX).padStart(RGB_HEX_LENGTH, '0')
}

private fun String.toEscapedScriptValue(): String =
    buildString {
        for (char in this@toEscapedScriptValue) {
            when {
                char == '\\' -> append("\\\\")

                char == '"' -> append("\\\"")

                char == '<' || char == '>' || char == '&' ||
                    char.code == LINE_SEPARATOR_CODE ||
                    char.code == PARAGRAPH_SEPARATOR_CODE ||
                    char.isISOControl() -> {
                    append("\\u")
                    append(char.code.toString(HEX_RADIX).padStart(UNICODE_HEX_LENGTH, '0'))
                }

                else -> append(char)
            }
        }
    }

private const val LINE_SEPARATOR_CODE = 0x2028
private const val PARAGRAPH_SEPARATOR_CODE = 0x2029
private const val RGB_MASK = 0xFFFFFF
private const val HEX_RADIX = 16
private const val RGB_HEX_LENGTH = 6
private const val UNICODE_HEX_LENGTH = 4
