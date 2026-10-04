package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.model.tag.TagDetail

public fun TagDetail.toLocal(): TagDetailLocalEntity =
    TagDetailLocalEntity(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )

public fun TagDetailLocalEntity.toDomain(): TagDetail =
    TagDetail(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )
