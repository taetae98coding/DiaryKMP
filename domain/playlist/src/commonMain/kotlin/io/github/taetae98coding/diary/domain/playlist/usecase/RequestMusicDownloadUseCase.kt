package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadManager
import org.koin.core.annotation.Factory

@Factory
public class RequestMusicDownloadUseCase internal constructor(
    private val musicDownloadManager: MusicDownloadManager,
) : UseCase<ListSort, Unit>() {
    override suspend fun execute(parameter: ListSort) {
        musicDownloadManager.requestDownload(sort = parameter)
    }
}
