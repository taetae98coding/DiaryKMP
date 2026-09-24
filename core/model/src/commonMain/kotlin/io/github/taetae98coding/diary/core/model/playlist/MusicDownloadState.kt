package io.github.taetae98coding.diary.core.model.playlist

public sealed interface MusicDownloadState {
    public data object Pending : MusicDownloadState

    public data class Running(
        val progress: Float?,
    ) : MusicDownloadState

    public data object Done : MusicDownloadState

    public data object Failed : MusicDownloadState
}
