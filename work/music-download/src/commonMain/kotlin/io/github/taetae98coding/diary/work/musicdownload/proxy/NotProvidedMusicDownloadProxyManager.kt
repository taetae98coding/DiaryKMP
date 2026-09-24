package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import io.github.taetae98coding.diary.domain.playlist.MusicDownloadProxyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal object NotProvidedMusicDownloadProxyManager : MusicDownloadProxyManager {
    override val status: Flow<MusicDownloadProxyStatus> = flowOf(MusicDownloadProxyStatus.NotProvided)
}
