package io.github.taetae98coding.diary.compose.map.google

import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.web.CAMERA_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.compose.map.web.PINS_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.PIN_MARKER_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.PIN_SELECTABLE_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.SPOT_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.SPOT_SELECTABLE_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.pinMarkerToScriptValue
import io.github.taetae98coding.diary.compose.map.web.toScriptValue

internal fun createGoogleMapHtml(
    camera: DiaryMapCamera?,
    spot: DiaryMapCoordinate? = null,
    isSpotSelectable: Boolean = false,
    pins: List<DiaryMapPin> = emptyList(),
    isPinSelectable: Boolean = false,
): String? =
    createGoogleMapHtml(
        apiKey =
            System
                .getProperty(GOOGLE_MAP_API_KEY_PROPERTY)
                .orEmpty(),
        camera = camera,
        spot = spot,
        isSpotSelectable = isSpotSelectable,
        pins = pins,
        isPinSelectable = isPinSelectable,
    )

internal fun createGoogleMapHtml(
    apiKey: String,
    camera: DiaryMapCamera?,
    spot: DiaryMapCoordinate? = null,
    isSpotSelectable: Boolean = false,
    pins: List<DiaryMapPin> = emptyList(),
    isPinSelectable: Boolean = false,
): String? {
    val normalizedKey = apiKey.trim()
    if (!normalizedKey.matches(API_KEY_PATTERN)) {
        return null
    }

    return GOOGLE_MAP_HTML_TEMPLATE
        .replace(API_KEY_PLACEHOLDER, normalizedKey)
        .replace(CAMERA_PLACEHOLDER, camera.toScriptValue())
        .replace(SPOT_PLACEHOLDER, spot.toScriptValue())
        .replace(SPOT_SELECTABLE_PLACEHOLDER, isSpotSelectable.toScriptValue())
        .replace(PINS_PLACEHOLDER, pins.toScriptValue())
        .replace(PIN_SELECTABLE_PLACEHOLDER, isPinSelectable.toScriptValue())
        .replace(PIN_MARKER_PLACEHOLDER, pinMarkerToScriptValue())
}

private const val GOOGLE_MAP_API_KEY_PROPERTY =
    "io.github.taetae98coding.diary.googleMapApiKey"
private const val API_KEY_PLACEHOLDER = "{{GOOGLE_MAP_API_KEY}}"
private val API_KEY_PATTERN = Regex("[A-Za-z0-9_-]+")
private val GOOGLE_MAP_HTML_TEMPLATE: String by lazy {
    checkNotNull(MapHttpServer::class.java.getResourceAsStream("/google-map.html"))
        .bufferedReader()
        .use { it.readText() }
}
