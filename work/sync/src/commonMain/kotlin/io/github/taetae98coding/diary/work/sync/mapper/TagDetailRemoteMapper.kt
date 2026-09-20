package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagDetailRemoteEntity

internal fun TagDetailLocalEntity.toRemote(): TagDetailRemoteEntity =
    TagDetailRemoteEntity(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )

internal fun TagDetailRemoteEntity.toLocal(): TagDetailLocalEntity =
    TagDetailLocalEntity(
        emoji = emoji,
        title = title,
        description = description,
        color = color,
    )
