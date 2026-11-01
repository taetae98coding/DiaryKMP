package io.github.taetae98coding.diary.compose.map

import kotlin.math.abs

private const val CENTER_TOLERANCE_RATIO = 0.01
private const val FULL_LONGITUDE = 360.0

// 지도가 알려 오는 가운데는 옮기도록 지정한 좌표와 소수점 끝자리까지 같지 않을 수 있어, 보이는 영역 크기에 비례한 오차 안이면 같은 곳으로 본다.
public fun isMapCentered(
    center: DiaryMapCoordinate?,
    bounds: DiaryMapBounds?,
    target: DiaryMapCoordinate,
): Boolean =
    when {
        center == null -> false

        bounds == null -> center == target

        else ->
            abs(center.latitude - target.latitude) <= abs(bounds.north - bounds.south) * CENTER_TOLERANCE_RATIO &&
                longitudeDistance(center.longitude, target.longitude) <= bounds.longitudeSpan() * CENTER_TOLERANCE_RATIO
    }

// 날짜변경선을 사이에 둔 두 경도는 반대 방향으로 재야 실제 거리가 된다.
private fun longitudeDistance(
    from: Double,
    to: Double,
): Double {
    val distance = abs(from - to).mod(FULL_LONGITUDE)
    return minOf(distance, FULL_LONGITUDE - distance)
}

// 날짜변경선을 걸친 영역은 서쪽 경도가 동쪽 경도보다 크다.
private fun DiaryMapBounds.longitudeSpan(): Double = if (west > east) east - west + FULL_LONGITUDE else east - west

public fun DiaryMapState.isCentered(coordinate: DiaryMapCoordinate): Boolean = isMapCentered(center = this.coordinate, bounds = bounds, target = coordinate)
