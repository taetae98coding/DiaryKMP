package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity

internal fun ContactLocalEntity.toRemote(): ContactRemoteEntity =
    ContactRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isFavorite = isFavorite,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ContactRemoteEntity.toLocal(): ContactLocalEntity =
    ContactLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isFavorite = isFavorite,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
