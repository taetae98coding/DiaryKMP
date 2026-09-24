package io.github.taetae98coding.diary.work.musicdownload.proxy

internal sealed interface MusicDownloadProxyResponse {
    data object Rejected : MusicDownloadProxyResponse

    data object Failed : MusicDownloadProxyResponse

    data class Completed(
        val path: String,
    ) : MusicDownloadProxyResponse
}
