package io.github.taetae98coding.diary.feature.playlist.ui

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewMusic(
    title: String,
    artist: String,
    thumbnail: String = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
): Music =
    Music(
        id = Uuid.random(),
        detail =
            MusicDetail(
                link = "https://youtu.be/dQw4w9WgXcQ",
                title = title,
                artist = artist,
                thumbnail = thumbnail,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
