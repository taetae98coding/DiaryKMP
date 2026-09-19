package io.github.taetae98coding.diary.core.mapper.playlist

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicDetailRemoteEntity

public fun MusicDetail.toLocal(): MusicDetailLocalEntity =
    MusicDetailLocalEntity(
        title = title,
        artist = artist,
    )

public fun MusicDetailLocalEntity.toDomain(): MusicDetail =
    MusicDetail(
        title = title,
        artist = artist,
    )

public fun MusicDetailLocalEntity.toRemote(): MusicDetailRemoteEntity =
    MusicDetailRemoteEntity(
        title = title,
        artist = artist,
    )

public fun MusicDetailRemoteEntity.toLocal(): MusicDetailLocalEntity =
    MusicDetailLocalEntity(
        title = title,
        artist = artist,
    )
