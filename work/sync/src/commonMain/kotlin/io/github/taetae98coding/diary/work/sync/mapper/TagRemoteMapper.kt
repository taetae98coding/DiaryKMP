package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity

internal fun TagLocalEntity.toRemote(): TagRemoteEntity =
    TagRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun TagRemoteEntity.toLocal(): TagLocalEntity =
    TagLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
