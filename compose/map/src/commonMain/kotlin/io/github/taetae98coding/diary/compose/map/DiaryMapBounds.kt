package io.github.taetae98coding.diary.compose.map

internal const val FULL_LONGITUDE = 360.0
private const val HALF_LONGITUDE = 180.0

public data class DiaryMapBounds(
    val south: Double,
    val north: Double,
    val west: Double,
    val east: Double,
)

internal val DiaryMapBounds.isFinite: Boolean
    get() = south.isFinite() && north.isFinite() && west.isFinite() && east.isFinite()

// 지도가 회전하면 화면 모서리가 지리 방향과 맞지 않으므로 어느 모서리가 남·북·서·동쪽인지 가정하지 않는다.
// 경도는 가운데에서 잰 방향 있는 차이로 견주어, 화면이 날짜변경선을 실제로 걸칠 때만 서쪽이 동쪽보다 커진다.
internal fun visibleBoundsOf(
    center: DiaryMapCoordinate,
    corners: List<DiaryMapCoordinate>,
): DiaryMapBounds =
    DiaryMapBounds(
        south = corners.minOf { corner -> corner.latitude },
        north = corners.maxOf { corner -> corner.latitude },
        west = corners.minBy { corner -> longitudeOffset(from = center.longitude, to = corner.longitude) }.longitude,
        east = corners.maxBy { corner -> longitudeOffset(from = center.longitude, to = corner.longitude) }.longitude,
    )

private fun longitudeOffset(
    from: Double,
    to: Double,
): Double = (to - from + HALF_LONGITUDE).mod(FULL_LONGITUDE) - HALF_LONGITUDE
