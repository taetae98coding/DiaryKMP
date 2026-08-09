package io.github.taetae98coding.diary.compose.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewPlace(
    title: String,
    color: Long,
    latitude: Double,
    longitude: Double,
): Place =
    Place(
        id = Uuid.random(),
        detail =
            PlaceDetail(
                title = title,
                description = "",
                color = color,
                coordinate = Coordinate(latitude = latitude, longitude = longitude),
                address = "",
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
