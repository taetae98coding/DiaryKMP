package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadProxyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetMusicDownloadProxyStatusUseCase internal constructor(
    private val musicDownloadProxyManager: MusicDownloadProxyManager,
) : FlowUseCase<Unit, MusicDownloadProxyStatus>() {
    override fun execute(parameter: Unit): Flow<Result<MusicDownloadProxyStatus>> = musicDownloadProxyManager.status.map { status -> Result.success(status) }
}
