package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicRemoteEntity

internal fun MusicLocalEntity.toRemote(): MusicRemoteEntity =
    MusicRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun MusicRemoteEntity.toLocal(): MusicLocalEntity =
    MusicLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
