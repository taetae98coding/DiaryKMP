package io.github.taetae98coding.diary.domain.qr.content

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateOrNaN
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateText
import io.github.taetae98coding.diary.core.model.place.toPlacePrecision

public val QrContent.Location.coordinate: Coordinate?
    get() {
        val latitude = latitudeOrNull() ?: return null
        val longitude = longitudeOrNull() ?: return null

        return Coordinate(latitude = latitude, longitude = longitude)
    }

internal fun QrContent.Location.latitudeOrNull(): Double? = parsedCoordinate().latitude.takeIf { value -> value in LATITUDE_RANGE }

internal fun QrContent.Location.longitudeOrNull(): Double? = parsedCoordinate().longitude.takeIf { value -> value in LONGITUDE_RANGE }

internal fun QrContent.Location.encodeLocation(): String {
    val (latitudeText, longitudeText) = coordinateTexts()

    return "geo:$latitudeText,$longitudeText"
}

internal fun QrContent.Location.coordinateTexts(): Pair<String, String> {
    val coordinate = coordinate ?: return latitude.trim() to longitude.trim()

    return coordinate.latitude.toGeoText() to coordinate.longitude.toGeoText()
}

private fun QrContent.Location.parsedCoordinate(): Coordinate =
    Coordinate(
        latitude = latitude.toPlaceCoordinateOrNaN(),
        longitude = longitude.toPlaceCoordinateOrNaN(),
    ).toPlacePrecision()

private val LATITUDE_RANGE = -90.0..90.0

private val LONGITUDE_RANGE = -180.0..180.0

private fun Double.toGeoText(): String {
    val text =
        toPlaceCoordinateText()
            .trimEnd('0')
            .trimEnd('.')

    return if (text == "-0") "0" else text
}
