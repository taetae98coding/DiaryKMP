package io.github.taetae98coding.diary.work.musicdownload.tool

internal interface DownloadToolPreparer {
    suspend fun prepare(): DownloadToolPrepareResult
}
