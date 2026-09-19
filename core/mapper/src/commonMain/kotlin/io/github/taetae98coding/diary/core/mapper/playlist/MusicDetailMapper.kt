package io.github.taetae98coding.diary.core.mapper.playlist

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicDetailRemoteEntity

public fun MusicDetail.toLocal(): MusicDetailLocalEntity =
    MusicDetailLocalEntity(
        link = link,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
    )

public fun MusicDetailLocalEntity.toDomain(): MusicDetail =
    MusicDetail(
        link = link,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
    )

public fun MusicDetailLocalEntity.toRemote(): MusicDetailRemoteEntity =
    MusicDetailRemoteEntity(
        link = link,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
    )

public fun MusicDetailRemoteEntity.toLocal(): MusicDetailLocalEntity =
    MusicDetailLocalEntity(
        link = link,
        title = title,
        artist = artist,
        thumbnail = thumbnail,
    )
