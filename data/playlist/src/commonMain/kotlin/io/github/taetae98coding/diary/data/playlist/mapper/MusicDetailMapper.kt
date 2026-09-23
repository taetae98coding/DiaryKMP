package io.github.taetae98coding.diary.data.playlist.mapper

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail

// 썸네일 컬럼은 뒤에 쓸 계획으로 남겨 두었지만 지금 곡은 썸네일을 링크에서 정하므로 저장하지도 읽지도 않는다.
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
