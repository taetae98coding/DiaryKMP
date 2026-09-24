package io.github.taetae98coding.diary.work.musicdownload.tool

internal interface MusicDownloader {
    suspend fun download(
        ytDlpPath: String,
        link: String,
        path: String,
        onProgress: suspend (Float) -> Unit,
    ): Boolean
}
