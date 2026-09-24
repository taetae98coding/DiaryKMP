package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath
import io.github.taetae98coding.diary.work.musicdownload.work.testVideoId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private const val YT_DLP_PATH = "/opt/homebrew/bin/yt-dlp"
private const val SUCCESS_EXIT_CODE = 0
private const val FAILURE_EXIT_CODE = 1
private const val FIRST_PHASE_CEILING = 0.9F
private val STREAM = byteArrayOf(1, 2, 3, 4)

class YtDlpDownloaderTest :
    BehaviorSpec({
        Given("yt-dlp가 있고 내려받기가 성공한다") {
            When("영상을 받으면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-003 가장 좋은 화질의 영상과 소리를 하나의 mp4로 합치도록 영상 ID의 링크를 맡긴다") {
                    val videoId = testVideoId()
                    val path = tempMusicFilePath(videoId = videoId)
                    val commandRunner = commandRunner(exitCode = SUCCESS_EXIT_CODE)
                    val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = Dispatchers.Default)

                    downloader.download(videoId = videoId, path = path, onProgress = {})

                    coVerify(exactly = 1) {
                        commandRunner.run(
                            commandList =
                                listOf(
                                    YT_DLP_PATH,
                                    "-f",
                                    "bv*+ba/b",
                                    "--merge-output-format",
                                    "mp4",
                                    "--no-playlist",
                                    "--newline",
                                    "--no-colors",
                                    "-o",
                                    path.downloading,
                                    "https://www.youtube.com/watch?v=$videoId",
                                ),
                            onLine = any(),
                        )
                    }
                }

                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-009 받는 도중에는 완성된 이름의 파일이 없고 끝난 뒤에만 완성된 이름이 된다") {
                    val videoId = testVideoId()
                    val path = tempMusicFilePath(videoId = videoId)
                    var completedExistsWhileDownloading = true
                    val commandRunner = mockk<CommandRunner>()
                    every { commandRunner.find(command = DownloadTool.YT_DLP.command) } returns YT_DLP_PATH
                    coEvery { commandRunner.run(commandList = any(), onLine = any()) } coAnswers
                        {
                            Path.of(path.downloading).writeBytes(STREAM)
                            completedExistsWhileDownloading = Path.of(path.completed).exists()
                            SUCCESS_EXIT_CODE
                        }
                    val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = Dispatchers.Default)

                    val isDownloaded = downloader.download(videoId = videoId, path = path, onProgress = {})

                    isDownloaded shouldBe true
                    completedExistsWhileDownloading shouldBe false
                    Path.of(path.completed).readBytes() shouldBe STREAM
                    Path.of(path.downloading).exists() shouldBe false
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-002 진행 중이 된 때부터 백분율을 알린다") {
                    val videoId = testVideoId()
                    val path = tempMusicFilePath(videoId = videoId)
                    val progressList = mutableListOf<Float>()
                    val commandRunner = commandRunner(exitCode = SUCCESS_EXIT_CODE, lineList = listOf("[download]  50.0% of 10MiB"))
                    val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = Dispatchers.Default)

                    downloader.download(videoId = videoId, path = path, onProgress = { value -> progressList += value })

                    progressList shouldContainInOrder listOf(0F, 0.5F * FIRST_PHASE_CEILING)
                }
            }
        }

        Given("내려받기가 실패로 끝난다") {
            When("영상을 받으면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-006 TC-MUSIC-DOWNLOAD-PROXY-DATA-006 받다 만 파일을 남기지 않는다") {
                    val videoId = testVideoId()
                    val path = tempMusicFilePath(videoId = videoId)
                    val commandRunner = commandRunner(exitCode = FAILURE_EXIT_CODE)
                    val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = Dispatchers.Default)

                    val isDownloaded = downloader.download(videoId = videoId, path = path, onProgress = {})

                    isDownloaded shouldBe false
                    Path.of(path.downloading).exists() shouldBe false
                    Path.of(path.completed).exists() shouldBe false
                }
            }
        }

        Given("yt-dlp가 없다") {
            When("영상을 받으면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-005 실행하지 않고 실패로 끝난다") {
                    val videoId = testVideoId()
                    val commandRunner = mockk<CommandRunner>()
                    every { commandRunner.find(command = any()) } returns null
                    val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = Dispatchers.Default)

                    downloader.download(videoId = videoId, path = tempMusicFilePath(videoId = videoId), onProgress = {}) shouldBe false

                    coVerify(exactly = 0) { commandRunner.run(commandList = any(), onLine = any()) }
                }
            }
        }
    })

internal fun tempMusicFilePath(videoId: String): MusicFilePath {
    val directory = createTempDirectory("diary-music")

    return MusicFilePath(
        downloading = directory.resolve("$videoId.downloading.mp4").toString(),
        completed = directory.resolve("$videoId.mp4").toString(),
    )
}

private fun commandRunner(
    exitCode: Int,
    lineList: List<String> = emptyList(),
): CommandRunner {
    val commandRunner = mockk<CommandRunner>()
    every { commandRunner.find(command = DownloadTool.YT_DLP.command) } returns YT_DLP_PATH
    coEvery { commandRunner.run(commandList = any(), onLine = any()) } coAnswers {
        val commandList = firstArg<List<String>>()
        val onLine = secondArg<suspend (String) -> Unit>()
        val outputPath = commandList[commandList.indexOf("-o") + 1]

        lineList.forEach { line -> onLine(line) }
        if (exitCode == SUCCESS_EXIT_CODE) Path.of(outputPath).writeBytes(STREAM)

        exitCode
    }

    return commandRunner
}

private fun List<Float>.shouldContainInOrder(expected: List<Float>) {
    filter { value -> value in expected } shouldContainExactly expected
}
