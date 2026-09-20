package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicDetailRemoteEntity

internal fun MusicDetailLocalEntity.toRemote(): MusicDetailRemoteEntity =
    MusicDetailRemoteEntity(
        link = link,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
    )

internal fun MusicDetailRemoteEntity.toLocal(): MusicDetailLocalEntity =
    MusicDetailLocalEntity(
        link = link,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
    )
