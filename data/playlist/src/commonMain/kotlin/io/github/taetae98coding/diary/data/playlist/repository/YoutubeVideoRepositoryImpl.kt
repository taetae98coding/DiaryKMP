package io.github.taetae98coding.diary.data.playlist.repository

import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtubenetwork.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.data.playlist.mapper.toDomain
import io.github.taetae98coding.diary.domain.playlist.repository.YoutubeVideoRepository
import org.koin.core.annotation.Factory

@Factory
internal class YoutubeVideoRepositoryImpl(
    private val youtubeVideoRemoteDataSource: YoutubeVideoRemoteDataSource,
) : YoutubeVideoRepository {
    override suspend fun fetch(link: String): YoutubeVideo = youtubeVideoRemoteDataSource.fetch(link = link).toDomain()
}
