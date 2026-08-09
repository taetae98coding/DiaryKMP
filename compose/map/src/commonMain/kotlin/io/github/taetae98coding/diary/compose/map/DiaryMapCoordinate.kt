package io.github.taetae98coding.diary.compose.map

public data class DiaryMapCoordinate(
    val latitude: Double,
    val longitude: Double,
)

internal val DiaryMapCoordinate.isFinite: Boolean
    get() = latitude.isFinite() && longitude.isFinite()
