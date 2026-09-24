package io.github.taetae98coding.diary.core.model.playlist

public sealed interface MusicDownloadProxyStatus {
    public data object NotProvided : MusicDownloadProxyStatus

    public data object Unavailable : MusicDownloadProxyStatus

    public data class Serving(
        val addressList: List<String>,
    ) : MusicDownloadProxyStatus
}
