package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity

internal fun WebTagLocalEntity.toRemote(): WebTagRemoteEntity =
    WebTagRemoteEntity(
        webId = webId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun WebTagRemoteEntity.toLocal(): WebTagLocalEntity =
    WebTagLocalEntity(
        webId = webId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
