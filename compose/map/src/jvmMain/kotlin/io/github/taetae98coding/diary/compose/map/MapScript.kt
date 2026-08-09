package io.github.taetae98coding.diary.compose.map

internal const val CAMERA_PLACEHOLDER = "{{DIARY_MAP_CAMERA}}"
internal const val SPOT_PLACEHOLDER = "{{DIARY_MAP_SPOT}}"
internal const val SPOT_SELECTABLE_PLACEHOLDER = "{{DIARY_MAP_SPOT_SELECTABLE}}"
internal const val PINS_PLACEHOLDER = "{{DIARY_MAP_PINS}}"
internal const val PIN_SELECTABLE_PLACEHOLDER = "{{DIARY_MAP_PIN_SELECTABLE}}"
internal const val PIN_MARKER_PLACEHOLDER = "{{DIARY_MAP_PIN_MARKER}}"

internal fun DiaryMapCamera?.toScriptValue(): String {
    if (this == null || !isFinite) return NO_VALUE_SCRIPT_VALUE

    return """{ "latitude": $latitude, "longitude": $longitude, "zoom": $zoom }"""
}

internal fun DiaryMapCoordinate?.toScriptValue(): String {
    if (this == null || !isFinite) return NO_VALUE_SCRIPT_VALUE

    return """{ "latitude": $latitude, "longitude": $longitude }"""
}

internal fun Boolean.toScriptValue(): String = if (this) "true" else "false"

private const val NO_VALUE_SCRIPT_VALUE = "null"
