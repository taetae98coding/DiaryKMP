package io.github.taetae98coding.diary.domain.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.location.CoordinateCircle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min

internal fun CoordinateBounds.toPlaceSearchBias(): CoordinateCircle? {
    // 날짜변경선에 걸친 영역은 서쪽 경도가 동쪽 경도보다 크므로 한 바퀴를 더해 동서 폭을 구한다.
    val longitudeSpan = if (east >= west) east - west else east - west + FULL_ANGLE_DEGREE
    val center =
        Coordinate(
            latitude = (south + north) / 2,
            longitude = (west + longitudeSpan / 2).normalizeLongitude(),
        )

    val halfHeightMeters = (north - south) / 2 * METERS_PER_LATITUDE_DEGREE
    val halfWidthMeters = longitudeSpan / 2 * METERS_PER_LATITUDE_DEGREE * cos(center.latitude * PI / STRAIGHT_ANGLE_DEGREE)
    val radiusMeters = min(halfHeightMeters, halfWidthMeters)
    val isUsable = center.isRepresentable && radiusMeters.isFinite() && radiusMeters > 0.0

    return CoordinateCircle(
        center = center,
        radiusMeters = radiusMeters,
    ).takeIf { isUsable }
}

private fun Double.normalizeLongitude(): Double = if (this > STRAIGHT_ANGLE_DEGREE) this - FULL_ANGLE_DEGREE else this

private const val METERS_PER_LATITUDE_DEGREE = 111_320.0
private const val STRAIGHT_ANGLE_DEGREE = 180.0
private const val FULL_ANGLE_DEGREE = 360.0
