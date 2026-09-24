package io.github.taetae98coding.diary.feature.playlist.ui.home

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import kotlin.uuid.Uuid

internal data class PlaylistHomeDownloadUiState(
    val stateMap: Map<Uuid, MusicDownloadState> = emptyMap(),
) {
    val isDownloading: Boolean =
        stateMap.values.any { state ->
            state is MusicDownloadState.Pending || state is MusicDownloadState.Running
        }
}
