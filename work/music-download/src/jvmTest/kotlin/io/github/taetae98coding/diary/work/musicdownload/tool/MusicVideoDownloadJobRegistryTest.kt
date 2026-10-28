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

class MusicVideoDownloadJobRegistryTest :
    BehaviorSpec({
        Given("한 영상을 받는 중이다") {
            When("같은 영상을 다시 요청하면") {
                Then("TC-89076056-a077-4e05-bcab-b567f7e2d906 새로 받지 않고 같은 결과를 함께 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any()) } coAnswers { gate.await() }
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        val first = async { registry.download(videoId = videoId, path = testMusicFilePath(videoId)) }
                        advanceUntilIdle()
                        val second = async { registry.download(videoId = videoId, path = testMusicFilePath(videoId)) }
                        advanceUntilIdle()
                        gate.complete(true)
                        advanceUntilIdle()

                        first.await() shouldBe true
                        second.await() shouldBe true
                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = videoId, path = any()) }
                    }
                }

                Then("TC-4b91dc4e-ba16-476b-906b-7dab9ef4c368 작업이 실패하면 합류한 요청 모두 실패 결과를 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any()) } coAnswers { gate.await() }
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        val first = async { registry.download(videoId = videoId, path = testMusicFilePath(videoId)) }
                        advanceUntilIdle()
                        val second = async { registry.download(videoId = videoId, path = testMusicFilePath(videoId)) }
                        advanceUntilIdle()
                        gate.complete(false)
                        advanceUntilIdle()

                        first.await() shouldBe false
                        second.await() shouldBe false
                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = videoId, path = any()) }
                    }
                }
            }

            When("같은 영상의 작업에 합류하면") {
                Then("TC-89076056-a077-4e05-bcab-b567f7e2d906 새로 받지 않고 진행 중인 작업의 결과를 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any()) } coAnswers { gate.await() }
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        launch { registry.download(videoId = videoId, path = testMusicFilePath(videoId)) }
                        advanceUntilIdle()
                        val joined = async { registry.join(videoId = videoId) }
                        advanceUntilIdle()
                        gate.complete(true)
                        advanceUntilIdle()

                        joined.await() shouldBe true
                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = videoId, path = any()) }
                    }
                }
            }

            When("다른 영상을 요청하면") {
                Then("TC-eaa26512-a870-4325-9960-a9b020fb8aab 첫 영상이 끝나기를 기다리지 않고 함께 받는다") {
                    runTest {
                        val first = testVideoId()
                        val second = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any()) } coAnswers { gate.await() }
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        launch { registry.download(videoId = first, path = testMusicFilePath(first)) }
                        launch { registry.download(videoId = second, path = testMusicFilePath(second)) }
                        advanceUntilIdle()

                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = first, path = any()) }
                        coVerify(exactly = 1) { ytDlpDownloader.download(videoId = second, path = any()) }

                        gate.complete(true)
                        advanceUntilIdle()
                    }
                }
            }

            When("요청한 쪽이 기다리기를 그만두면") {
                Then("TC-1eb83d1c-c85f-43ce-b342-6dfc0d159761 받기를 멈추지 않고 끝까지 받는다") {
                    runTest {
                        val videoId = testVideoId()
                        val gate = CompletableDeferred<Boolean>()
                        var isCompleted = false
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any()) } coAnswers
                            {
                                val result = gate.await()
                                isCompleted = true
                                result
                            }
                        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler) + CoroutineExceptionHandler { _, _ -> })
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = scope)

                        val request = launch { registry.download(videoId = videoId, path = testMusicFilePath(videoId)) }
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

        Given("받는 중인 영상이 없다") {
            When("그 영상의 작업에 합류하면") {
                Then("합류할 작업이 없음을 알리고 새로 받지 않는다") {
                    runTest {
                        val ytDlpDownloader = mockk<YtDlpDownloader>()
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        registry.join(videoId = testVideoId()) shouldBe null

                        coVerify(exactly = 0) { ytDlpDownloader.download(videoId = any(), path = any()) }
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
                        coEvery { ytDlpDownloader.download(videoId = any(), path = any()) } returns false
                        val registry = registry(ytDlpDownloader = ytDlpDownloader, scope = this)

                        registry.download(videoId = videoId, path = testMusicFilePath(videoId)) shouldBe false
                        registry.download(videoId = videoId, path = testMusicFilePath(videoId)) shouldBe false

                        coVerify(exactly = 2) { ytDlpDownloader.download(videoId = videoId, path = any()) }
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
