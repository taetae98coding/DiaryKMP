package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadManager
import org.koin.core.annotation.Factory

@Factory
public class FindMusicDownloadSupportUseCase internal constructor(
    private val musicDownloadManager: MusicDownloadManager,
) : UseCase<Unit, Boolean>() {
    override suspend fun execute(parameter: Unit): Boolean = musicDownloadManager.isSupported
}
