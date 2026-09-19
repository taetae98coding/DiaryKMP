package io.github.taetae98coding.diary.domain.playlist.repository

import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo

public interface YoutubeVideoRepository {
    public suspend fun fetch(link: String): YoutubeVideo
}
