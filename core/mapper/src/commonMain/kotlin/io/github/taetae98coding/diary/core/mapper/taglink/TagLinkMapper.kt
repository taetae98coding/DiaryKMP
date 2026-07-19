package io.github.taetae98coding.diary.core.mapper.taglink

import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity

public fun TagLinkLocalEntity.toRemote(): TagLinkRemoteEntity =
    TagLinkRemoteEntity(
        fromTagId = fromTagId,
        toTagId = toTagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun TagLinkRemoteEntity.toLocal(): TagLinkLocalEntity =
    TagLinkLocalEntity(
        fromTagId = fromTagId,
        toTagId = toTagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
