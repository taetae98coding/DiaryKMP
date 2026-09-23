package io.github.taetae98coding.diary.data.playlist.mapper

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail

internal fun MusicDetail.toLocal(): MusicDetailLocalEntity =
    MusicDetailLocalEntity(
        link = link,
        title = title,
        artist = artist,
        thumbnail = "",
    )

internal fun MusicDetailLocalEntity.toDomain(): MusicDetail =
    MusicDetail(
        title = title,
        artist = artist,
        link = link,
    )
