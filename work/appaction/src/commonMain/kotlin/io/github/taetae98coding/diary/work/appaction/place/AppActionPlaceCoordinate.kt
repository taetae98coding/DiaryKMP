package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateOrNaN
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument

private val LATITUDE_RANGE = -90.0..90.0
private val LONGITUDE_RANGE = -180.0..180.0

// 숫자로 읽을 수 없는 값은 NaN으로 넘겨, 데스크톱 MCP 도구와 같이 앱 기능이 올바르지 않은 좌표로 실패하게 한다.
internal fun String.toCoordinateArgument(): Double = toPlaceCoordinateOrNaN()

internal fun String?.toCoordinateArgumentOrNull(): Double? = this?.toCoordinateArgument()

// 지도는 항상 올바른 영역을 넘기므로 UseCase가 영역을 검사하지 않는다. 그래서 동작이 받은 영역은 여기서 확인한다.
internal fun coordinateBoundsArgument(
    south: String,
    north: String,
    west: String,
    east: String,
): CoordinateBounds {
    val bounds =
        CoordinateBounds(
            south = south.toCoordinateArgument(),
            north = north.toCoordinateArgument(),
            west = west.toCoordinateArgument(),
            east = east.toCoordinateArgument(),
        )

    if (bounds.south !in LATITUDE_RANGE || bounds.north !in LATITUDE_RANGE) invalidArgument(message = "'south' and 'north' must be latitudes between -90 and 90.")
    if (bounds.west !in LONGITUDE_RANGE || bounds.east !in LONGITUDE_RANGE) invalidArgument(message = "'west' and 'east' must be longitudes between -180 and 180.")
    if (bounds.north < bounds.south) invalidArgument(message = "'north' must not be below 'south'.")

    return bounds
}
