package io.github.taetae98coding.diary.feature.tag.ui

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewTag(
    emoji: String,
    title: String,
    color: Long,
): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = emoji, title = title, description = "태그 설명", color = color),
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
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
