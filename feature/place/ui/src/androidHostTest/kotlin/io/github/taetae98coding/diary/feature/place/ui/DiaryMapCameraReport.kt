package io.github.taetae98coding.diary.feature.place.ui

import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState

private const val CAMERA_CLASS_NAME = "io.github.taetae98coding.diary.compose.map.DiaryMapCamera"
private const val MOVE_CAMERA_PREFIX = "moveCamera$"
private const val REPORTED_ZOOM = 15.0

/**
 * 지도가 보고 있는 위치와 보이는 영역을 알려 오는 것을 흉내 낸다.
 *
 * 보이는 영역은 지도 제공자의 표시 요소만 `:compose:map`의 internal 함수로 알려 올 수 있고 호스트 테스트 환경은 그 표시 요소를 만들지 못하므로,
 * 표시 요소가 부르는 같은 함수를 리플렉션으로 부른다.
 */
internal fun DiaryMapState.reportCamera(
    center: DiaryMapCoordinate,
    bounds: DiaryMapBounds,
    zoom: Double = REPORTED_ZOOM,
) {
    val cameraClass = Class.forName(CAMERA_CLASS_NAME)
    val camera =
        cameraClass
            .getConstructor(Double::class.java, Double::class.java, Double::class.java, DiaryMapBounds::class.java)
            .newInstance(center.latitude, center.longitude, zoom, bounds)
    val moveCamera = DiaryMapState::class.java.methods.single { method -> method.name.startsWith(MOVE_CAMERA_PREFIX) }

    moveCamera.invoke(this, camera)
}

internal fun boundsAround(
    center: DiaryMapCoordinate,
    span: Double,
): DiaryMapBounds =
    DiaryMapBounds(
        south = center.latitude - span / 2,
        north = center.latitude + span / 2,
        west = center.longitude - span / 2,
        east = center.longitude + span / 2,
    )
