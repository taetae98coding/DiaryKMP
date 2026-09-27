package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetMusicDownloadStateUseCase internal constructor(
    private val musicDownloadManager: MusicDownloadManager,
) : FlowUseCase<Unit, Map<MusicDownloadTarget, MusicDownloadState>>() {
    override fun execute(parameter: Unit): Flow<Result<Map<MusicDownloadTarget, MusicDownloadState>>> = musicDownloadManager.stateMap.map { stateMap -> Result.success(stateMap) }
}
