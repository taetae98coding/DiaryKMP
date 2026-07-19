package io.github.taetae98coding.diary.core.mapper.tag

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagDetailRemoteEntity

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

public fun TagDetailLocalEntity.toRemote(): TagDetailRemoteEntity =
    TagDetailRemoteEntity(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )

public fun TagDetailRemoteEntity.toLocal(): TagDetailLocalEntity =
    TagDetailLocalEntity(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )
