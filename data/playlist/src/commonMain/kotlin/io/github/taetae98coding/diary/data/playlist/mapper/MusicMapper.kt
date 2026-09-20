package io.github.taetae98coding.diary.data.playlist.mapper

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.Music

internal fun Music.toLocal(): MusicLocalEntity =
    MusicLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun MusicLocalEntity.toDomain(): Music =
    Music(
        id = id,
        detail = detail.toDomain(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
