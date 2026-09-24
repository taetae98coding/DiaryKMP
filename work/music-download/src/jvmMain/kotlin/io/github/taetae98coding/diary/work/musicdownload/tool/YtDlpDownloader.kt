package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.process.SUCCESS_EXIT_CODE
import org.koin.core.annotation.Factory

@Factory
internal class YtDlpDownloader(
    private val commandRunner: CommandRunner,
) : MusicDownloader {
    override suspend fun download(
        ytDlpPath: String,
        link: String,
        path: String,
        onProgress: suspend (Float) -> Unit,
    ): Boolean {
        val progress = YtDlpProgress()
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
                        path,
                        link,
                    ),
            ) { line ->
                progress.onLine(line = line)?.let { value -> onProgress(value) }
            }

        return exitCode == SUCCESS_EXIT_CODE
    }
}
