package io.github.taetae98coding.diary.compose.map

import kotlin.math.abs

private const val CENTER_TOLERANCE_RATIO = 0.01

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
                abs(center.longitude - target.longitude) <= abs(bounds.east - bounds.west) * CENTER_TOLERANCE_RATIO
    }

public fun DiaryMapState.isCentered(coordinate: DiaryMapCoordinate): Boolean = isMapCentered(center = this.coordinate, bounds = bounds, target = coordinate)
