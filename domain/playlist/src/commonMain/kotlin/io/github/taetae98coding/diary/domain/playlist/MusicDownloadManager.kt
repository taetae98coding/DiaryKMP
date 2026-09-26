package io.github.taetae98coding.diary.domain.playlist

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import kotlinx.coroutines.flow.Flow

public interface MusicDownloadManager {
    public val isSupported: Boolean

    public val stateMap: Flow<Map<MusicDownloadTarget, MusicDownloadState>>

    public val event: Flow<MusicDownloadEvent>

    public fun requestDownload(sort: ListSort)
}
