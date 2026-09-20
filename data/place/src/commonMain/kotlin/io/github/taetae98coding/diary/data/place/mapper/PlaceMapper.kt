package io.github.taetae98coding.diary.data.place.mapper

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place

internal fun Place.toLocal(): PlaceLocalEntity =
    PlaceLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun PlaceLocalEntity.toDomain(): Place =
    Place(
        id = id,
        detail = detail.toDomain(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
