@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.musicdownload.scheduler

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.work.MusicDownloadWork
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

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
                        val id = Uuid.random()
                        val gate = CompletableDeferred<Unit>()
                        val stateHolder = MusicDownloadStateHolder()
                        val work = mockk<MusicDownloadWork>()
                        coEvery { work.doWork(sort = any()) } coAnswers
                            {
                                stateHolder.update(id = id, state = MusicDownloadState.Running(progress = 0.62F))
                                gate.await()
                            }
                        val scheduler = scheduler(work = work, stateHolder = stateHolder, scope = this)

                        scheduler.download(sort = ListSort.TITLE)
                        advanceUntilIdle()
                        scheduler.download(sort = ListSort.RECENTLY_UPDATED)
                        advanceUntilIdle()

                        stateHolder.stateMap.value shouldBe mapOf(id to MusicDownloadState.Running(progress = 0.62F))

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
                        val pending = Uuid.random()
                        val running = Uuid.random()
                        val done = Uuid.random()
                        val failed = Uuid.random()
                        val stateHolder = MusicDownloadStateHolder()
                        val work = mockk<MusicDownloadWork>()
                        coEvery { work.doWork(sort = any()) } coAnswers
                            {
                                stateHolder.update(id = pending, state = MusicDownloadState.Pending)
                                stateHolder.update(id = running, state = MusicDownloadState.Running(progress = 0.5F))
                                stateHolder.update(id = done, state = MusicDownloadState.Done)
                                stateHolder.update(id = failed, state = MusicDownloadState.Failed)
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
    }) {
    public companion object {
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
