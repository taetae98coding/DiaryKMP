package io.github.taetae98coding.diary.core.model.place

import io.github.taetae98coding.diary.core.model.location.Coordinate

public data class PlaceDetail(
    val title: String,
    val description: String,
    val color: Long,
    val coordinate: Coordinate,
    val address: String,
) {
    public companion object {
        public val EMPTY: PlaceDetail =
            PlaceDetail(
                title = "",
                description = "",
                color = 0L,
                coordinate = Coordinate(latitude = Double.NaN, longitude = Double.NaN),
                address = "",
            )
    }
}
