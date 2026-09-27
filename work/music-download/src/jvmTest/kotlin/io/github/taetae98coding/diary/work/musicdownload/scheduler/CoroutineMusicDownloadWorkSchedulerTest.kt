@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.musicdownload.scheduler

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.file.api.datasource.AppFileLocalDataSource
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicDownloadTargetUseCase
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadEventHolder
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPrepareResult
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPreparer
import io.github.taetae98coding.diary.work.musicdownload.tool.MusicDownloader
import io.github.taetae98coding.diary.work.musicdownload.work.MusicDownloadWork
import io.github.taetae98coding.diary.work.musicdownload.work.MusicDownloadWorkImpl
import io.github.taetae98coding.diary.work.musicdownload.work.testDownloadTarget
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class CoroutineMusicDownloadWorkSchedulerTest :
    BehaviorSpec({
        Given("진행 중인 다운로드가 없다") {
            When("다운로드를 예약한다") {
                Then("TC-PLAYLIST-HOME-FEATURE-019 작업을 한 번 실행한다") {
                    runTest {
                        val work = mockk<MusicDownloadWork>()
                        coEvery { work.doWork(sort = any()) } returns Unit
                        val scheduler = scheduler(work = work, scope = this)

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()

                        coVerify(exactly = 1) { work.doWork(sort = ListSort.TITLE) }
                    }
                }
            }
        }

        Given("이미 실행 중인 다운로드가 있다") {
            When("다운로드를 다시 예약한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-004 예약을 하나로 유지하고 새로 시작하지 않는다") {
                    runTest {
                        val gate = CompletableDeferred<Unit>()
                        val work = mockk<MusicDownloadWork>()
                        coEvery { work.doWork(sort = any()) } coAnswers { gate.await() }
                        val scheduler = scheduler(work = work, scope = this)

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()

                        coVerify(exactly = 1) { work.doWork(sort = any()) }

                        gate.complete(Unit)
                        advanceUntilIdle()
                    }
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-006 진행 중인 곡의 상태가 그대로 남는다") {
                    runTest {
                        val target = testDownloadTarget()
                        val gate = CompletableDeferred<Unit>()
                        val stateHolder = MusicDownloadStateHolder()
                        val work = mockk<MusicDownloadWork>()
                        coEvery { work.doWork(sort = any()) } coAnswers
                            {
                                stateHolder.update(target = target, state = MusicDownloadState.Running(progress = 0.62F))
                                gate.await()
                            }
                        val scheduler = scheduler(work = work, stateHolder = stateHolder, scope = this)

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        scheduler.download(sort = ListSort.RECENTLY_UPDATED)
                        advanceUntilIdle()

                        stateHolder.stateMap.value shouldBe mapOf(target to MusicDownloadState.Running(progress = 0.62F))

                        gate.complete(Unit)
                        advanceUntilIdle()
                    }
                }
            }
        }

        Given("앞선 다운로드가 끝났다") {
            When("다운로드를 다시 예약한다") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-005 작업을 다시 실행한다") {
                    runTest {
                        val work = mockk<MusicDownloadWork>()
                        coEvery { work.doWork(sort = any()) } returns Unit
                        val scheduler = scheduler(work = work, scope = this)

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()

                        coVerify(exactly = 2) { work.doWork(sort = ListSort.TITLE) }
                    }
                }
            }
        }

        Given("작업이 대기 상태를 남긴 채 끝난다") {
            When("다운로드가 끝나면") {
                Then("완료와 실패만 남기고 대기와 진행 중을 정리한다") {
                    runTest {
                        val pending = testDownloadTarget()
                        val running = testDownloadTarget()
                        val done = testDownloadTarget()
                        val failed = testDownloadTarget()
                        val stateHolder = MusicDownloadStateHolder()
                        val work = mockk<MusicDownloadWork>()
                        coEvery { work.doWork(sort = any()) } coAnswers
                            {
                                stateHolder.update(target = pending, state = MusicDownloadState.Pending)
                                stateHolder.update(target = running, state = MusicDownloadState.Running(progress = 0.5F))
                                stateHolder.update(target = done, state = MusicDownloadState.Done)
                                stateHolder.update(target = failed, state = MusicDownloadState.Failed)
                                error("download interrupted")
                            }
                        // 실행 수단은 실패를 전용 스코프의 핸들러에 맡기므로 테스트 스코프가 아닌 그런 스코프에 예약한다.
                        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler) + CoroutineExceptionHandler { _, _ -> })
                        val scheduler = scheduler(work = work, stateHolder = stateHolder, scope = scope)

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()

                        stateHolder.stateMap.value shouldBe
                            mapOf(
                                done to MusicDownloadState.Done,
                                failed to MusicDownloadState.Failed,
                            )
                    }
                }
            }
        }

        Given("제목순으로 실행한 다운로드의 첫째 곡이 진행 중인 채 멈춰 있다") {
            When("정렬을 바꿔 다운로드를 다시 실행하고 멈춰 있던 곡이 끝나면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-015 처음 실행할 때의 순서대로 받고 바꾼 정렬로 대상을 다시 판정하지 않는다") {
                    runTest {
                        val targetList = listOf(testDownloadTarget(), testDownloadTarget(), testDownloadTarget())
                        val gate = CompletableDeferred<Unit>()
                        val downloadedList = mutableListOf<MusicDownloadTarget>()
                        val findTargetUseCase = findTargetUseCase(result = Result.success(targetList))
                        val downloader = mockk<MusicDownloader>()
                        coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                            {
                                val target = firstArg<MusicDownloadTarget>()
                                if (target == targetList.first()) gate.await()
                                downloadedList += target
                                true
                            }
                        val scheduler =
                            scheduler(
                                work = realWork(findMusicDownloadTargetUseCase = findTargetUseCase, musicDownloader = downloader),
                                scope = this,
                            )

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        scheduler.download(sort = ListSort.RECENTLY_UPDATED)
                        advanceUntilIdle()
                        gate.complete(Unit)
                        advanceUntilIdle()

                        downloadedList shouldContainExactly targetList
                        coVerify(exactly = 1) { findTargetUseCase(parameter = any()) }
                        coVerify(exactly = 0) { findTargetUseCase(parameter = ListSort.RECENTLY_UPDATED) }
                    }
                }
            }
        }

        Given("영상 A를 가리키던 곡이 진행 중인 채 멈춰 있고 그 곡의 링크가 영상 B로 바뀌었다") {
            When("다운로드를 한 번 더 실행하고 멈춰 있던 곡이 끝나면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-022 대상을 다시 판정하지 않고 영상 A만 한 번 받으며 영상 B는 받지 않는다") {
                    runTest {
                        val targetA = testDownloadTarget()
                        val targetB = testDownloadTarget().copy(id = targetA.id)
                        val gate = CompletableDeferred<Unit>()
                        val findTargetUseCase = mockk<FindMusicDownloadTargetUseCase>()
                        coEvery { findTargetUseCase(parameter = any()) } returnsMany listOf(Result.success(listOf(targetA)), Result.success(listOf(targetB)))
                        val downloader = mockk<MusicDownloader>()
                        coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } coAnswers
                            {
                                gate.await()
                                true
                            }
                        val scheduler =
                            scheduler(
                                work = realWork(findMusicDownloadTargetUseCase = findTargetUseCase, musicDownloader = downloader),
                                scope = this,
                            )

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        gate.complete(Unit)
                        advanceUntilIdle()

                        coVerify(exactly = 1) { findTargetUseCase(parameter = any()) }
                        coVerify(exactly = 1) { downloader.download(target = targetA, path = any(), onProgress = any()) }
                        coVerify(exactly = 0) { downloader.download(target = targetB, path = any(), onProgress = any()) }
                    }
                }
            }
        }

        Given("다운로드를 실행해 내려받기 준비가 끝나지 않은 채 멈춰 있다") {
            When("다운로드를 한 번 더 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-016 준비가 한 번만 일어나고 곡은 한 번씩만 받는다") {
                    runTest {
                        val target = testDownloadTarget()
                        val gate = CompletableDeferred<Unit>()
                        val preparer = mockk<DownloadToolPreparer>()
                        coEvery { preparer.prepare() } coAnswers
                            {
                                gate.await()
                                DownloadToolPrepareResult.Prepared
                            }
                        val downloader = succeedingDownloader()
                        val scheduler =
                            scheduler(
                                work =
                                    realWork(
                                        downloadToolPreparer = preparer,
                                        findMusicDownloadTargetUseCase = findTargetUseCase(result = Result.success(listOf(target))),
                                        musicDownloader = downloader,
                                    ),
                                scope = this,
                            )

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        gate.complete(Unit)
                        advanceUntilIdle()

                        coVerify(exactly = 1) { preparer.prepare() }
                        coVerify(exactly = 1) { downloader.download(target = target, path = any(), onProgress = any()) }
                    }
                }
            }
        }

        Given("내려받기 준비는 성공하고 대상 곡을 판정하지 못한다") {
            When("다운로드를 실행하면") {
                Then("TC-MUSIC-DOWNLOAD-DOMAIN-018 알리지 않고 어떤 곡도 받지 않은 채 끝나며 다시 실행할 수 있다") {
                    runTest {
                        val stateHolder = MusicDownloadStateHolder()
                        val eventHolder = MusicDownloadEventHolder()
                        val findTargetUseCase = findTargetUseCase(result = Result.failure(IllegalStateException("account unavailable")))
                        val downloader = succeedingDownloader()
                        // 실행 수단은 실패를 전용 스코프의 핸들러에 맡기므로 테스트 스코프가 아닌 그런 스코프에 예약한다.
                        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler) + CoroutineExceptionHandler { _, _ -> })
                        val scheduler =
                            scheduler(
                                work =
                                    realWork(
                                        findMusicDownloadTargetUseCase = findTargetUseCase,
                                        musicDownloader = downloader,
                                        musicDownloadStateHolder = stateHolder,
                                        musicDownloadEventHolder = eventHolder,
                                    ),
                                stateHolder = stateHolder,
                                scope = scope,
                            )

                        eventHolder.event.test {
                            scheduler.download(sort = ListSort.TITLE)
                            advanceUntilIdle()

                            expectNoEvents()
                        }
                        stateHolder.stateMap.value shouldBe emptyMap()
                        coVerify(exactly = 0) { downloader.download(target = any(), path = any(), onProgress = any()) }

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()

                        coVerify(exactly = 2) { findTargetUseCase(parameter = ListSort.TITLE) }
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun findTargetUseCase(result: Result<List<MusicDownloadTarget>>): FindMusicDownloadTargetUseCase {
            val useCase = mockk<FindMusicDownloadTargetUseCase>()
            coEvery { useCase(parameter = any()) } returns result
            return useCase
        }

        private fun succeedingDownloader(): MusicDownloader {
            val downloader = mockk<MusicDownloader>()
            coEvery { downloader.download(target = any(), path = any(), onProgress = any()) } returns true
            return downloader
        }

        private fun realWork(
            findMusicDownloadTargetUseCase: FindMusicDownloadTargetUseCase,
            musicDownloader: MusicDownloader,
            downloadToolPreparer: DownloadToolPreparer =
                mockk<DownloadToolPreparer>().apply {
                    coEvery { prepare() } returns DownloadToolPrepareResult.Prepared
                },
            musicDownloadStateHolder: MusicDownloadStateHolder = MusicDownloadStateHolder(),
            musicDownloadEventHolder: MusicDownloadEventHolder = MusicDownloadEventHolder(),
        ): MusicDownloadWorkImpl {
            val fileDataSource = mockk<AppFileLocalDataSource>()
            coEvery { fileDataSource.exists(directory = any(), name = any()) } returns false
            coEvery { fileDataSource.resolve(directory = any(), name = any()) } coAnswers { "/tmp/music/${secondArg<String>()}" }
            coEvery { fileDataSource.delete(directory = any(), name = any()) } returns Unit

            return MusicDownloadWorkImpl(
                downloadToolPreparer = downloadToolPreparer,
                musicDownloader = musicDownloader,
                findMusicDownloadTargetUseCase = findMusicDownloadTargetUseCase,
                appFileLocalDataSource = fileDataSource,
                musicDownloadStateHolder = musicDownloadStateHolder,
                musicDownloadEventHolder = musicDownloadEventHolder,
            )
        }

        private fun scheduler(
            work: MusicDownloadWork,
            scope: CoroutineScope,
            stateHolder: MusicDownloadStateHolder = MusicDownloadStateHolder(),
        ): CoroutineMusicDownloadWorkScheduler =
            CoroutineMusicDownloadWorkScheduler(
                musicDownloadWork = work,
                musicDownloadStateHolder = stateHolder,
                scope = scope,
            )
    }
}
