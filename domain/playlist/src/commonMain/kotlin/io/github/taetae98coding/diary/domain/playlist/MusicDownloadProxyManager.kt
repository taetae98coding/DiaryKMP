package io.github.taetae98coding.diary.domain.playlist

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import kotlinx.coroutines.flow.Flow

public interface MusicDownloadProxyManager {
    public val status: Flow<MusicDownloadProxyStatus>
}
