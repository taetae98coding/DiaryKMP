package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.domain.playlist.link.toYoutubeVideoLink
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadDispatcher
import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.process.SUCCESS_EXIT_CODE
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath
import io.github.taetae98coding.diary.work.musicdownload.work.complete
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
internal class YtDlpDownloader(
    private val commandRunner: CommandRunner,
    @param:MusicDownloadDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun download(
        videoId: String,
        path: MusicFilePath,
    ): Boolean {
        val ytDlpPath = commandRunner.find(command = DownloadTool.YT_DLP.command) ?: return false

        val exitCode =
            commandRunner.run(
                commandList =
                    listOf(
                        ytDlpPath,
                        // 가장 좋은 화질의 영상과 소리를 받아 하나의 mp4로 합친다.
                        "-f",
                        "bv*+ba/b",
                        "--merge-output-format",
                        "mp4",
                        // 링크에 재생 목록이 함께 붙어 있어도 그 영상 하나만 받는다.
                        "--no-playlist",
                        "-o",
                        path.downloading,
                        videoId.toYoutubeVideoLink(),
                    ),
            )

        return withContext(dispatcher) { path.complete(isDownloaded = exitCode == SUCCESS_EXIT_CODE) }
    }
}
