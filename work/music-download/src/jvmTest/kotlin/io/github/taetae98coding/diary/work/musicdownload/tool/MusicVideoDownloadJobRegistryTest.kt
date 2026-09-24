@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.musicdownload.tool

import io.github.taetae98coding.diary.work.musicdownload.work.testMusicFilePath
import io.github.taetae98coding.diary.work.musicdownload.work.testVideoId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

private const val PROGRESS_ARGUMENT_INDEX = 2

class MusicVideoDownloadJobRegistryTest :
    BehaviorSpec({
        Given("한 영상을 받는 중이다") {
            When("같은 영상을 다시 요청하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-007 새로 받지 않고 같은 결과를 함께 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any(), onProgress = any()) } coAnswers { gate.await() }
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        val first = async { registry.download(videoId = videoId, path = testMusicFilePath(videoId), onProgress = {}) }
                        advanceUntilIdle()
                        val second = async { registry.download(videoId = videoId, path = testMusicFilePath(videoId), onProgress = {}) }
                        advanceUntilIdle()
                        gate.complete(true)
                        advanceUntilIdle()

                        first.await() shouldBe true
                        second.await() shouldBe true
                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = videoId, path = any(), onProgress = any()) }
                    }
                }

                Then("합류한 요청도 진행률을 함께 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        var report: suspend (Float) -> Unit = {}
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any(), onProgress = any()) } coAnswers
                            {
                                report = arg(PROGRESS_ARGUMENT_INDEX)
                                gate.await()
                            }
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)
                        val joinedProgressList = mutableListOf<Float>()

                        launch { registry.download(videoId = videoId, path = testMusicFilePath(videoId), onProgress = {}) }
                        advanceUntilIdle()
                        launch { registry.download(videoId = videoId, path = testMusicFilePath(videoId), onProgress = { value -> joinedProgressList += value }) }
                        advanceUntilIdle()
                        report(0.62F)
                        advanceUntilIdle()
                        gate.complete(true)
                        advanceUntilIdle()

                        joinedProgressList shouldBe listOf(0.62F)
                    }
                }
            }

            When("다른 영상을 요청하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-010 첫 영상이 끝나기를 기다리지 않고 함께 받는다") {
                    runTest {
                        val first = testVideoId()
                        val second = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any(), onProgress = any()) } coAnswers { gate.await() }
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        launch { registry.download(videoId = first, path = testMusicFilePath(first), onProgress = {}) }
                        launch { registry.download(videoId = second, path = testMusicFilePath(second), onProgress = {}) }
                        advanceUntilIdle()

                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = first, path = any(), onProgress = any()) }
                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = second, path = any(), onProgress = any()) }

                        gate.complete(true)
                        advanceUntilIdle()
                    }
                }
            }

            When("요청한 쪽이 기다리기를 그만두면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-008 받기를 멈추지 않고 끝까지 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        var isCompleted = false
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any(), onProgress = any()) } coAnswers
                            {
                                val result = gate.await()
                                isCompleted = true
                                result
                            }
                        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler) + CoroutineExceptionHandler { _, _ -> })
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = scope)

                        val request = launch { registry.download(videoId = videoId, path = testMusicFilePath(videoId), onProgress = {}) }
                        advanceUntilIdle()
                        request.cancel()
                        advanceUntilIdle()
                        gate.complete(true)
                        advanceUntilIdle()

                        isCompleted shouldBe true
                    }
                }
            }
        }

        Given("앞선 작업이 끝났다") {
            When("같은 영상을 다시 요청하면") {
                Then("새 작업으로 다시 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any(), onProgress = any()) } returns false
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        registry.download(videoId = videoId, path = testMusicFilePath(videoId), onProgress = {}) shouldBe false
                        registry.download(videoId = videoId, path = testMusicFilePath(videoId), onProgress = {}) shouldBe false

                        coVerify(exactly = 2) { ytDlpDownloader.download(videoId = videoId, path = any(), onProgress = any()) }
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun registry(
            ytDlpDownloader: YtDlpDownloader,
            scope: CoroutineScope,
        ): MusicVideoDownloadJobRegistry =
            MusicVideoDownloadJobRegistry(
                ytDlpDownloader = ytDlpDownloader,
                scope = scope,
            )
    }
}
