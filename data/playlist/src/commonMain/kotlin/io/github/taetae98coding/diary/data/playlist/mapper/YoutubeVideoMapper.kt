package io.github.taetae98coding.diary.data.playlist.mapper

import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtube.network.api.entity.YoutubeVideoRemoteEntity

internal fun YoutubeVideoRemoteEntity.toDomain(): YoutubeVideo =
    YoutubeVideo(
        title = title,
        channelName = authorName,
    )
