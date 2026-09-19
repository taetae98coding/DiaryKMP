package io.github.taetae98coding.diary.feature.playlist.ui

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewMusic(
    title: String,
    artist: String,
): Music =
    Music(
        id = Uuid.random(),
        detail =
            MusicDetail(
                title = title,
                artist = artist,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
