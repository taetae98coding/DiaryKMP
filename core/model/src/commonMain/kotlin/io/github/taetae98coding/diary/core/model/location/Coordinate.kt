package io.github.taetae98coding.diary.core.model.location

public data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    public val isRepresentable: Boolean
        get() = latitude in LATITUDE_RANGE && longitude in LONGITUDE_RANGE

    private companion object {
        private val LATITUDE_RANGE = -90.0..90.0
        private val LONGITUDE_RANGE = -180.0..180.0
    }
}
