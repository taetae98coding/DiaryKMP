package io.github.taetae98coding.diary.data.tag.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.model.tag.TagDetail

internal fun TagDetail.toLocal(): TagDetailLocalEntity =
    TagDetailLocalEntity(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )

internal fun TagDetailLocalEntity.toDomain(): TagDetail =
    TagDetail(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )
