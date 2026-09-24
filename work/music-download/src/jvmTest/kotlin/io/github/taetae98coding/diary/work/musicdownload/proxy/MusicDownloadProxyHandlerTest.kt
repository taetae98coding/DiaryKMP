package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource
import io.github.taetae98coding.diary.work.musicdownload.process.CommandRunner
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadTool
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicVideoDownloadJobRegistry
import io.github.taetae98coding.diary.work.musicdownload.work.testVideoId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class MusicDownloadProxyHandlerTest :
    BehaviorSpec({
        Given("영상 ID의 형태를 만족하지 않는 값이다") {
            When("요청을 처리하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-002 거절하고 어떤 영상도 받지 않는다") {
                    val invalidList = listOf("abcdefghij", "abcdefghijkl", "abcdefghi!k", "abcdefghi k", "")

                    invalidList.forEach { value ->
                        val registry = registry(result = true)
                        val handler = handler(registry = registry)

                        handler.handle(videoId = value) shouldBe MusicDownloadProxyResponse.Rejected

                        coVerify(exactly = 0) { registry.download(videoId = any(), path = any(), onProgress = any()) }
                    }
                }
            }
        }

        Given("요청한 영상의 파일이 이미 있다") {
            When("요청을 처리하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-003 받지 않고 그 파일을 전달한다") {
                    val videoId = testVideoId()
                    val registry = registry(result = true)
                    val handler = handler(registry = registry, exists = true)

                    handler.handle(videoId = videoId) shouldBe MusicDownloadProxyResponse.Completed(path = "/tmp/music/$videoId.mp4")

                    coVerify(exactly = 0) { registry.download(videoId = any(), path = any(), onProgress = any()) }
                }
            }
        }

        Given("파일이 없고 도구가 모두 있으며 내려받기가 성공한다") {
            When("요청을 처리하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-004 받아 합친 뒤 완성된 파일을 전달한다") {
                    val videoId = testVideoId()
                    val registry = registry(result = true)
                    val handler = handler(registry = registry)

                    handler.handle(videoId = videoId) shouldBe MusicDownloadProxyResponse.Completed(path = "/tmp/music/$videoId.mp4")

                    coVerify(exactly = 1) { registry.download(videoId = videoId, path = any(), onProgress = any()) }
                }
            }
        }

        Given("내려받기 도구가 없다") {
            When("요청을 처리하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-005 설치하지도 받지도 않고 실패로 응답한다") {
                    val caseList =
                        listOf(
                            listOf(DownloadTool.FFMPEG),
                            listOf(DownloadTool.YT_DLP),
                            emptyList(),
                        )

                    caseList.forEach { installedList ->
                        val registry = registry(result = true)
                        val commandRunner = commandRunner(installedList = installedList)
                        val handler = handler(registry = registry, commandRunner = commandRunner)

                        handler.handle(videoId = testVideoId()) shouldBe MusicDownloadProxyResponse.Failed

                        coVerify(exactly = 0) { registry.download(videoId = any(), path = any(), onProgress = any()) }
                        coVerify(exactly = 0) { commandRunner.run(commandList = any(), onLine = any()) }
                    }
                }
            }
        }

        Given("내려받기가 실패한다") {
            When("요청을 처리하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-006 실패로 응답한다") {
                    val handler = handler(registry = registry(result = false))

                    handler.handle(videoId = testVideoId()) shouldBe MusicDownloadProxyResponse.Failed
                }
            }
        }
    })

private fun registry(result: Boolean): MusicVideoDownloadJobRegistry {
    val registry = mockk<MusicVideoDownloadJobRegistry>()
    coEvery { registry.download(videoId = any(), path = any(), onProgress = any()) } returns result

    return registry
}

private fun commandRunner(installedList: List<DownloadTool> = listOf(DownloadTool.YT_DLP, DownloadTool.FFMPEG)): CommandRunner {
    val commandRunner = mockk<CommandRunner>()
    coEvery { commandRunner.find(command = any()) } coAnswers {
        val command = firstArg<String>()
        "/opt/homebrew/bin/$command".takeIf { installedList.any { tool -> tool.command == command } }
    }

    return commandRunner
}

private fun handler(
    registry: MusicVideoDownloadJobRegistry,
    commandRunner: CommandRunner = commandRunner(),
    exists: Boolean = false,
): MusicDownloadProxyHandler {
    val fileDataSource = mockk<AppFileLocalDataSource>()
    coEvery { fileDataSource.exists(directory = any(), name = any()) } returns exists
    coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers { "/tmp/music/${secondArg<String>()}" }

    return MusicDownloadProxyHandler(
        commandRunner = commandRunner,
        appFileLocalDataSource = fileDataSource,
        musicVideoDownloadJobRegistry = registry,
    )
}
