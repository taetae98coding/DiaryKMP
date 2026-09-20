package io.github.taetae98coding.diary.data.tag.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.tag.Tag

internal fun Tag.toLocal(): TagLocalEntity =
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
