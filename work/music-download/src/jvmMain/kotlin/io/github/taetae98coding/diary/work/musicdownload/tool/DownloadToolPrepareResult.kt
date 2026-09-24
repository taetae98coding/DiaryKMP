package io.github.taetae98coding.diary.work.musicdownload.tool

internal sealed interface DownloadToolPrepareResult {
    data class Prepared(
        val ytDlpPath: String,
    ) : DownloadToolPrepareResult

    data object NotInstalled : DownloadToolPrepareResult

    data object Failed : DownloadToolPrepareResult
}
