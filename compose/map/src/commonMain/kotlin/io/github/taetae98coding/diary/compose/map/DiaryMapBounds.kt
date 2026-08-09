package io.github.taetae98coding.diary.compose.map

public data class DiaryMapBounds(
    val south: Double,
    val north: Double,
    val west: Double,
    val east: Double,
)

internal val DiaryMapBounds.isFinite: Boolean
    get() = south.isFinite() && north.isFinite() && west.isFinite() && east.isFinite()
