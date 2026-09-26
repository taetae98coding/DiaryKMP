package io.github.taetae98coding.diary.feature.playlist.ui.home

import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.playlist.link.toMusicDownloadTargetOrNull

internal data class PlaylistHomeDownloadUiState(
    val stateMap: Map<MusicDownloadTarget, MusicDownloadState> = emptyMap(),
) {
    val isDownloading: Boolean =
        stateMap.values.any { state ->
            state is MusicDownloadState.Pending || state is MusicDownloadState.Running
        }

    fun stateOf(music: Music): MusicDownloadState? = music.toMusicDownloadTargetOrNull()?.let { target -> stateMap[target] }
}
