package io.github.taetae98coding.diary.domain.playlist.link

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget

public fun Music.toMusicDownloadTargetOrNull(): MusicDownloadTarget? {
    val videoId = detail.link.toYoutubeVideoIdOrNull() ?: return null

    return MusicDownloadTarget(id = id, videoId = videoId)
}
