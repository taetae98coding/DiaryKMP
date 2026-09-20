package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity

internal fun TagLinkLocalEntity.toRemote(): TagLinkRemoteEntity =
    TagLinkRemoteEntity(
        fromTagId = fromTagId,
        toTagId = toTagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun TagLinkRemoteEntity.toLocal(): TagLinkLocalEntity =
    TagLinkLocalEntity(
        fromTagId = fromTagId,
        toTagId = toTagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
