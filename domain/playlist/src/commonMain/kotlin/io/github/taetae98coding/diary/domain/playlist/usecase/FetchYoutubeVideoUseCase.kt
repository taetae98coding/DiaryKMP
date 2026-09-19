package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.YoutubeVideo
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.link.toYoutubeVideoLinkOrThrow
import io.github.taetae98coding.diary.domain.playlist.repository.YoutubeVideoRepository
import org.koin.core.annotation.Factory

@Factory
public class FetchYoutubeVideoUseCase internal constructor(
    private val youtubeVideoRepository: YoutubeVideoRepository,
) : UseCase<String, YoutubeVideo>() {
    override suspend fun execute(parameter: String): YoutubeVideo = youtubeVideoRepository.fetch(link = parameter.toYoutubeVideoLinkOrThrow())
}
