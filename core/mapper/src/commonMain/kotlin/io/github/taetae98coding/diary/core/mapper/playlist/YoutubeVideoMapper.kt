package io.github.taetae98coding.diary.core.mapper.playlist

import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtubenetwork.api.entity.YoutubeVideoRemoteEntity

public fun YoutubeVideoRemoteEntity.toDomain(): YoutubeVideo =
    YoutubeVideo(
        title = title,
        channelName = authorName,
        thumbnail = thumbnailUrl,
    )
