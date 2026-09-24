package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetMusicDownloadEventUseCase internal constructor(
    private val musicDownloadManager: MusicDownloadManager,
) : FlowUseCase<Unit, MusicDownloadEvent>() {
    override fun execute(parameter: Unit): Flow<Result<MusicDownloadEvent>> = musicDownloadManager.event.map { event -> Result.success(event) }
}
