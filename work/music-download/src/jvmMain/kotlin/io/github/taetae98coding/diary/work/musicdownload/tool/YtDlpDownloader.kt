package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.domain.playlist.link.toYoutubeVideoLink
import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadDispatcher
import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.process.SUCCESS_EXIT_CODE
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Factory

@Factory
internal class YtDlpDownloader(
    private val commandRunner: CommandRunner,
    @param:MusicDownloadDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun download(
        videoId: String,
        path: MusicFilePath,
        onProgress: suspend (Float) -> Unit,
    ): Boolean {
        val ytDlpPath = commandRunner.find(command = DownloadTool.YT_DLP.command) ?: return false
        val progress = YtDlpProgress()

        onProgress(0F)

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
                        // 진행률을 한 줄씩 받아 읽으려면 덮어쓰기 출력을 꺼야 한다.
                        "--newline",
                        "--no-colors",
                        "-o",
                        path.downloading,
                        videoId.toYoutubeVideoLink(),
                    ),
            ) { line ->
                progress.onLine(line = line)?.let { value -> onProgress(value) }
            }

        return withContext(dispatcher) { path.complete(isDownloaded = exitCode == SUCCESS_EXIT_CODE) }
    }
}

private fun MusicFilePath.complete(isDownloaded: Boolean): Boolean {
    val downloadingPath = Path(downloading)

    if (isDownloaded && SystemFileSystem.exists(downloadingPath)) {
        SystemFileSystem.atomicMove(source = downloadingPath, destination = Path(completed))
        return true
    }

    SystemFileSystem.delete(path = downloadingPath, mustExist = false)
    return false
}
