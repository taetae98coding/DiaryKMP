package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.work.MusicFilePath
import io.github.taetae98coding.diary.work.musicdownload.work.testVideoId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private const val YT_DLP_PATH = "/opt/homebrew/bin/yt-dlp"
private const val SUCCESS_EXIT_CODE = 0
private const val FAILURE_EXIT_CODE = 1
private val STREAM = byteArrayOf(1, 2, 3, 4)

class YtDlpDownloaderTest :
    BehaviorSpec({
        Given("yt-dlp가 있고 내려받기가 성공한다") {
            When("영상을 받으면") {
                Then("TC-b73bcff4-7728-4616-b88d-3f3a740a0eba 가장 좋은 화질의 영상과 소리를 하나의 mp4로 합치도록 영상 ID의 링크를 맡긴다") {
                    runTest {
                        val videoId = testVideoId()
                        val path = tempMusicFilePath(videoId = videoId)
                        val commandRunner = commandRunner(exitCode = SUCCESS_EXIT_CODE)
                        val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = StandardTestDispatcher(testScheduler))

                        downloader.download(videoId = videoId, path = path)

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
                                        "-o",
                                        path.downloading,
                                        "https://www.youtube.com/watch?v=$videoId",
                                    ),
                            )
                        }
                    }
                }

                Then("TC-ee57b46c-6357-4700-8e5d-c963cb745ef5 받는 도중에는 완성된 이름의 파일이 없고 끝난 뒤에만 완성된 이름이 된다") {
                    runTest {
                        val videoId = testVideoId()
                        val path = tempMusicFilePath(videoId = videoId)
                        var completedExistsWhileDownloading = true
                        val commandRunner = mockk<CommandRunner>()
                        coEvery { commandRunner.find(command = DownloadTool.YT_DLP.command) } returns YT_DLP_PATH
                        coEvery { commandRunner.run(commandList = any()) } coAnswers
                            {
                                Path.of(path.downloading).writeBytes(STREAM)
                                completedExistsWhileDownloading = Path.of(path.completed).exists()
                                SUCCESS_EXIT_CODE
                            }
                        val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = StandardTestDispatcher(testScheduler))

                        val isDownloaded = downloader.download(videoId = videoId, path = path)

                        isDownloaded shouldBe true
                        completedExistsWhileDownloading shouldBe false
                        Path.of(path.completed).readBytes() shouldBe STREAM
                        Path.of(path.downloading).exists() shouldBe false
                    }
                }
            }
        }

        Given("내려받기가 실패로 끝난다") {
            When("영상을 받으면") {
                Then("TC-fb645f71-3566-4b33-ba55-307a8865ce79 TC-ec5526aa-e08c-44e8-b2ae-ac0b8cf43cc3 받다 만 파일을 남기지 않는다") {
                    runTest {
                        val videoId = testVideoId()
                        val path = tempMusicFilePath(videoId = videoId)
                        val commandRunner = commandRunner(exitCode = FAILURE_EXIT_CODE)
                        val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = StandardTestDispatcher(testScheduler))

                        val isDownloaded = downloader.download(videoId = videoId, path = path)

                        isDownloaded shouldBe false
                        Path.of(path.downloading).exists() shouldBe false
                        Path.of(path.completed).exists() shouldBe false
                    }
                }
            }
        }

        Given("yt-dlp가 없다") {
            When("영상을 받으면") {
                Then("TC-ee49eb44-ca70-4a6d-b8bb-d719c7f3f210 실행하지 않고 실패로 끝난다") {
                    runTest {
                        val videoId = testVideoId()
                        val commandRunner = mockk<CommandRunner>()
                        coEvery { commandRunner.find(command = any()) } returns null
                        val downloader = YtDlpDownloader(commandRunner = commandRunner, dispatcher = StandardTestDispatcher(testScheduler))

                        downloader.download(videoId = videoId, path = tempMusicFilePath(videoId = videoId)) shouldBe false

                        coVerify(exactly = 0) { commandRunner.run(commandList = any()) }
                    }
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

private fun commandRunner(exitCode: Int): CommandRunner {
    val commandRunner = mockk<CommandRunner>()
    coEvery { commandRunner.find(command = DownloadTool.YT_DLP.command) } returns YT_DLP_PATH
    coEvery { commandRunner.run(commandList = any()) } coAnswers {
        val commandList = firstArg<List<String>>()
        val outputPath = commandList[commandList.indexOf("-o") + 1]

        if (exitCode == SUCCESS_EXIT_CODE) Path.of(outputPath).writeBytes(STREAM)

        exitCode
    }

    return commandRunner
}
