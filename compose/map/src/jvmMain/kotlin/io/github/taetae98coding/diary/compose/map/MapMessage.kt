package io.github.taetae98coding.diary.compose.map

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlin.uuid.Uuid

internal sealed interface MapMessage {
    data object Ready : MapMessage

    data class Camera(
        val camera: DiaryMapCamera,
    ) : MapMessage

    data class Click(
        val coordinate: DiaryMapCoordinate,
    ) : MapMessage

    data class PinClick(
        val id: Uuid,
    ) : MapMessage
}

internal fun String.toMapMessageOrNull(): MapMessage? {
    val json = runCatching { Json.parseToJsonElement(this) }.getOrNull() as? JsonObject ?: return null

    return when (json.stringOrNull(TYPE_KEY)) {
        READY_TYPE -> MapMessage.Ready
        CAMERA_TYPE -> json.toCameraMessageOrNull()
        CLICK_TYPE -> json.toClickMessageOrNull()
        PIN_CLICK_TYPE -> json.toPinClickMessageOrNull()
        else -> null
    }
}

private fun JsonObject.toCameraMessageOrNull(): MapMessage.Camera? {
    val latitude = doubleOrNull(LATITUDE_KEY)
    val longitude = doubleOrNull(LONGITUDE_KEY)
    val zoom = doubleOrNull(ZOOM_KEY)

    if (latitude == null || longitude == null || zoom == null) return null

    return DiaryMapCamera(latitude = latitude, longitude = longitude, zoom = zoom, bounds = toBoundsOrNull())
        .takeIf { camera -> camera.isFinite }
        ?.let { camera -> MapMessage.Camera(camera = camera) }
}

private fun JsonObject.toBoundsOrNull(): DiaryMapBounds? {
    val south = doubleOrNull(SOUTH_KEY)
    val north = doubleOrNull(NORTH_KEY)
    val west = doubleOrNull(WEST_KEY)
    val east = doubleOrNull(EAST_KEY)

    if (listOf(south, north, west, east).any { value -> value == null }) return null

    return DiaryMapBounds(
        south = checkNotNull(south),
        north = checkNotNull(north),
        west = checkNotNull(west),
        east = checkNotNull(east),
    ).takeIf { bounds -> bounds.isFinite }
}

private fun JsonObject.toClickMessageOrNull(): MapMessage.Click? {
    val latitude = doubleOrNull(LATITUDE_KEY)
    val longitude = doubleOrNull(LONGITUDE_KEY)

    if (latitude == null || longitude == null) return null

    return DiaryMapCoordinate(latitude = latitude, longitude = longitude)
        .takeIf { coordinate -> coordinate.isFinite }
        ?.let { coordinate -> MapMessage.Click(coordinate = coordinate) }
}

private fun JsonObject.toPinClickMessageOrNull(): MapMessage.PinClick? =
    (get(ID_KEY) as? JsonPrimitive)
        ?.takeIf { primitive -> primitive.isString }
        ?.contentOrNull
        ?.let { id -> Uuid.parseOrNull(id) }
        ?.let { id -> MapMessage.PinClick(id = id) }

private fun JsonObject.doubleOrNull(key: String): Double? = (get(key) as? JsonPrimitive)?.doubleOrNull

private fun JsonObject.stringOrNull(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull

private const val TYPE_KEY = "type"
private const val READY_TYPE = "ready"
private const val CAMERA_TYPE = "camera"
private const val CLICK_TYPE = "click"
private const val PIN_CLICK_TYPE = "pinClick"
private const val ID_KEY = "id"
private const val LATITUDE_KEY = "latitude"
private const val LONGITUDE_KEY = "longitude"
private const val ZOOM_KEY = "zoom"
private const val SOUTH_KEY = "south"
private const val NORTH_KEY = "north"
private const val WEST_KEY = "west"
private const val EAST_KEY = "east"
