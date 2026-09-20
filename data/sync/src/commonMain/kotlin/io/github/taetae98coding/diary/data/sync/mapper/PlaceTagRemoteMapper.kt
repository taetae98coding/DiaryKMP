package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity

internal fun PlaceTagLocalEntity.toRemote(): PlaceTagRemoteEntity =
    PlaceTagRemoteEntity(
        placeId = placeId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun PlaceTagRemoteEntity.toLocal(): PlaceTagLocalEntity =
    PlaceTagLocalEntity(
        placeId = placeId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
