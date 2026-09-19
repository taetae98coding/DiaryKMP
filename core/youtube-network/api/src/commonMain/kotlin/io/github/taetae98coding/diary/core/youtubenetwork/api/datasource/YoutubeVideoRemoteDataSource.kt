package io.github.taetae98coding.diary.core.youtubenetwork.api.datasource

import io.github.taetae98coding.diary.core.youtubenetwork.api.entity.YoutubeVideoRemoteEntity

public interface YoutubeVideoRemoteDataSource {
    public suspend fun fetch(link: String): YoutubeVideoRemoteEntity
}
