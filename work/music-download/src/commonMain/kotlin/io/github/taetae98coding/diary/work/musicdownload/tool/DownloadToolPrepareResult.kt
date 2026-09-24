package io.github.taetae98coding.diary.work.musicdownload.tool

internal sealed interface DownloadToolPrepareResult {
    data object Prepared : DownloadToolPrepareResult

    data object NotInstalled : DownloadToolPrepareResult

    data object Failed : DownloadToolPrepareResult

    data object ProxyNotConfigured : DownloadToolPrepareResult

    data object ProxyUnreachable : DownloadToolPrepareResult
}
