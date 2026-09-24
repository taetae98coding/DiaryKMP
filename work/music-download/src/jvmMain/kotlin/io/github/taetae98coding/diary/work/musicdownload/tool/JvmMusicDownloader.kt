package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath
import org.koin.core.annotation.Factory

@Factory
internal class JvmMusicDownloader(
    private val musicVideoDownloadJobRegistry: MusicVideoDownloadJobRegistry,
) : MusicDownloader {
    override suspend fun download(
        target: MusicDownloadTarget,
        path: MusicFilePath,
        onProgress: suspend (Float) -> Unit,
    ): Boolean = musicVideoDownloadJobRegistry.download(videoId = target.videoId, path = path, onProgress = onProgress)
}
