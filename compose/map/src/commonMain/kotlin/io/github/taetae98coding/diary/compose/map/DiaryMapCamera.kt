package io.github.taetae98coding.diary.compose.map

internal data class DiaryMapCamera(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double,
    val bounds: DiaryMapBounds? = null,
) {
    companion object {
        const val NEIGHBORHOOD_ZOOM: Double = 15.0
    }
}

internal val DiaryMapCamera.isFinite: Boolean
    get() = latitude.isFinite() && longitude.isFinite() && zoom.isFinite()
