package io.github.taetae98coding.diary.feature.place.ui

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal val PREVIEW_PLACE_DETAIL: PlaceDetail =
    PlaceDetail(
        title = "장소 제목",
        description = "장소 설명",
        color = 0xFF3A7BD5,
        coordinate = Coordinate(latitude = 37.5665, longitude = 126.9780),
        address = "서울특별시 중구 세종대로 110",
    )

internal fun previewPlace(
    title: String,
    color: Long,
    latitude: Double,
    longitude: Double,
): Place =
    Place(
        id = Uuid.random(),
        detail =
            PREVIEW_PLACE_DETAIL.copy(
                title = title,
                color = color,
                coordinate = Coordinate(latitude = latitude, longitude = longitude),
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewSearchedPlace(
    name: String,
    latitude: Double,
    longitude: Double,
): SearchedPlace =
    SearchedPlace(
        id = Uuid.random(),
        name = name,
        address = "서울특별시 중구 세종대로 110",
        coordinate = Coordinate(latitude = latitude, longitude = longitude),
    )
