package io.github.taetae98coding.diary.data.playlist.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.core.youtube.network.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.data.playlist.mapper.toDomain
import io.github.taetae98coding.diary.domain.playlist.repository.YoutubeVideoRepository

@Inject
@ContributesBinding(AppScope::class)
internal class YoutubeVideoRepositoryImpl(
    private val youtubeVideoRemoteDataSource: YoutubeVideoRemoteDataSource,
) : YoutubeVideoRepository {
    override suspend fun fetch(link: String): YoutubeVideo = youtubeVideoRemoteDataSource.read(link = link).toDomain()
}
