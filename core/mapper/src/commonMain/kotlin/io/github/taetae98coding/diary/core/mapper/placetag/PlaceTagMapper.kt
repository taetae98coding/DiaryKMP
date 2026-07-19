package io.github.taetae98coding.diary.core.mapper.placetag

import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity

public fun PlaceTagLocalEntity.toRemote(): PlaceTagRemoteEntity =
    PlaceTagRemoteEntity(
        placeId = placeId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun PlaceTagRemoteEntity.toLocal(): PlaceTagLocalEntity =
    PlaceTagLocalEntity(
        placeId = placeId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
