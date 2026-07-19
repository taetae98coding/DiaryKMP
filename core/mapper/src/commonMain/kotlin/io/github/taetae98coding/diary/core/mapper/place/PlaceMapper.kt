package io.github.taetae98coding.diary.core.mapper.place

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity

public fun Place.toLocal(): PlaceLocalEntity =
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

public fun PlaceLocalEntity.toRemote(): PlaceRemoteEntity =
    PlaceRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun PlaceRemoteEntity.toLocal(): PlaceLocalEntity =
    PlaceLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
