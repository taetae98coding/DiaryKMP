package io.github.taetae98coding.diary.core.youtube.network.api.datasource

import io.github.taetae98coding.diary.core.youtube.network.api.entity.YoutubeVideoRemoteEntity

public interface YoutubeVideoRemoteDataSource {
    public suspend fun read(link: String): YoutubeVideoRemoteEntity
}
