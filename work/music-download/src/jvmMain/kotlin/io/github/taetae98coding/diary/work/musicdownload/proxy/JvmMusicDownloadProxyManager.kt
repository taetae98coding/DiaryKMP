package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadProxyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import org.koin.core.annotation.Factory

@Factory
internal class JvmMusicDownloadProxyManager(
    musicDownloadProxyServer: MusicDownloadProxyServer,
) : MusicDownloadProxyManager {
    override val status: Flow<MusicDownloadProxyStatus> = musicDownloadProxyServer.status.filterNotNull()
}
