package io.github.taetae98coding.diary.compose.music

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun previewMusic(
    title: String,
    artist: String,
    link: String = "https://youtu.be/dQw4w9WgXcQ",
): Music =
    Music(
        id = Uuid.random(),
        detail =
            MusicDetail(
                title = title,
                artist = artist,
                link = link,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
