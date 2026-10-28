package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.work.testDownloadTarget
import io.github.taetae98coding.diary.work.musicdownload.work.testMusicFilePath
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class JvmMusicDownloaderTest :
    BehaviorSpec({
        Given("데스크톱 앱에서 곡을 받는다") {
            When("받기를 마치면") {
                Then("TC-b3dc3f37-eecd-4741-a56b-de280770d85d 끝날 때까지 백분율을 알리지 않는다") {
                    runTest {
                        val target = testDownloadTarget()
                        val progressList = mutableListOf<Float>()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any()) } returns true
                        val downloader =
                            JvmMusicDownloader(
                                musicVideoDownloadJobRegistry = MusicVideoDownloadJobRegistry(ytDlpDownloader = ytDlpDownloader, scope = this),
                            )

                        val isDownloaded =
                            downloader.download(
                                target = target,
                                path = testMusicFilePath(target.videoId),
                                onProgress = { value -> progressList += value },
                            )

                        isDownloaded shouldBe true
                        progressList.shouldBeEmpty()
                    }
                }
            }
        }
    })
