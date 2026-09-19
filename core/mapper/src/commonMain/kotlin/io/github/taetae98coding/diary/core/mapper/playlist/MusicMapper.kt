package io.github.taetae98coding.diary.core.mapper.playlist

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicRemoteEntity

public fun Music.toLocal(): MusicLocalEntity =
    MusicLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MusicLocalEntity.toDomain(): Music =
    Music(
        id = id,
        detail = detail.toDomain(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MusicLocalEntity.toRemote(): MusicRemoteEntity =
    MusicRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MusicRemoteEntity.toLocal(): MusicLocalEntity =
    MusicLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
