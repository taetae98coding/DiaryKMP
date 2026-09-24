package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource
import io.github.taetae98coding.diary.domain.playlist.link.isYoutubeVideoId
import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicVideoDownloadJobRegistry
import io.github.taetae98coding.diary.work.musicdownload.tool.findMissingDownloadToolList
import io.github.taetae98coding.diary.work.musicdownload.work.MUSIC_FILE_DIRECTORY
import io.github.taetae98coding.diary.work.musicdownload.work.resolveMusicFilePath
import io.github.taetae98coding.diary.work.musicdownload.work.toMusicFileName
import org.koin.core.annotation.Factory

@Factory
internal class MusicDownloadProxyHandler(
    private val commandRunner: CommandRunner,
    private val appFileLocalDataSource: AppFileLocalDataSource,
    private val musicVideoDownloadJobRegistry: MusicVideoDownloadJobRegistry,
) {
    suspend fun handle(videoId: String): MusicDownloadProxyResponse {
        if (!videoId.isYoutubeVideoId()) return MusicDownloadProxyResponse.Rejected

        val path = appFileLocalDataSource.resolveMusicFilePath(videoId = videoId)

        return when {
            appFileLocalDataSource.exists(directory = MUSIC_FILE_DIRECTORY, name = videoId.toMusicFileName()) ->
                MusicDownloadProxyResponse.Completed(path = path.completed)

            commandRunner.findMissingDownloadToolList().isNotEmpty() -> MusicDownloadProxyResponse.Failed

            musicVideoDownloadJobRegistry.download(videoId = videoId, path = path, onProgress = {}) ->
                MusicDownloadProxyResponse.Completed(path = path.completed)

            else -> MusicDownloadProxyResponse.Failed
        }
    }
}
