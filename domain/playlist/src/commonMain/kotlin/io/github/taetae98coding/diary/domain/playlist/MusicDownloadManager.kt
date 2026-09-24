package io.github.taetae98coding.diary.domain.playlist

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface MusicDownloadManager {
    public val stateMap: Flow<Map<Uuid, MusicDownloadState>>

    public val event: Flow<MusicDownloadEvent>

    public fun requestDownload(sort: ListSort)
}
