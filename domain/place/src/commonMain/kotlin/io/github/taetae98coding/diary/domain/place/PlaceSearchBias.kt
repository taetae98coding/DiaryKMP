package io.github.taetae98coding.diary.domain.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.location.CoordinateCircle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min

/**
 * 보고 있는 지도 영역 안에 들어가는 가장 큰 원을 검색의 기준 위치로 만든다.
 * 기준 위치로 쓸 수 없는 영역에서는 기준 위치 없이 검색한다.
 */
internal fun CoordinateBounds.toPlaceSearchBias(): CoordinateCircle? {
    val center =
        Coordinate(
            latitude = (south + north) / 2,
            longitude = (west + east) / 2,
        )

    val halfHeightMeters = (north - south) / 2 * METERS_PER_LATITUDE_DEGREE
    val halfWidthMeters = (east - west) / 2 * METERS_PER_LATITUDE_DEGREE * cos(center.latitude * PI / STRAIGHT_ANGLE_DEGREE)
    val radiusMeters = min(halfHeightMeters, halfWidthMeters)
    val isUsable = center.isRepresentable && radiusMeters.isFinite() && radiusMeters > 0.0

    return CoordinateCircle(
        center = center,
        radiusMeters = radiusMeters,
    ).takeIf { isUsable }
}

private const val METERS_PER_LATITUDE_DEGREE = 111_320.0
private const val STRAIGHT_ANGLE_DEGREE = 180.0
