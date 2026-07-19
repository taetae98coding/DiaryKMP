package io.github.taetae98coding.diary.core.mapper.tag

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity

public fun Tag.toLocal(): TagLocalEntity =
    TagLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun TagLocalEntity.toDomain(): Tag =
    Tag(
        id = id,
        detail = detail.toDomain(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun TagLocalEntity.toRemote(): TagRemoteEntity =
    TagRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun TagRemoteEntity.toLocal(): TagLocalEntity =
    TagLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
