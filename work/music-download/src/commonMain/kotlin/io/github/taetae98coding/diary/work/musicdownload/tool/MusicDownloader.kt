package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath

internal interface MusicDownloader {
    suspend fun download(
        target: MusicDownloadTarget,
        path: MusicFilePath,
        onProgress: suspend (Float) -> Unit,
    ): Boolean
}
