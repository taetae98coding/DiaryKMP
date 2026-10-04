package io.github.taetae98coding.diary.compose.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun previewPlace(
    title: String,
    color: Long,
    latitude: Double = 37.5665,
    longitude: Double = 126.9780,
    description: String = "장소 설명",
    address: String = "서울특별시 중구 세종대로 110",
): Place =
    Place(
        id = Uuid.random(),
        detail =
            PlaceDetail(
                title = title,
                description = description,
                color = color,
                coordinate = Coordinate(latitude = latitude, longitude = longitude),
                address = address,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
