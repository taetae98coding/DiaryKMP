@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.musicdownload.scheduler

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.work.musicdownload.state.MusicDownloadStateHolder
import io.github.taetae98coding.diary.work.musicdownload.work.MusicDownloadWork
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class CoroutineMusicDownloadWorkSchedulerTest :
    BehaviorSpec({
        Given("진행 중인 다운로드가 없다") {
            When("다운로드를 예약한다") {
                Then("작업을 한 번 실행한다") {
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
                Then("예약을 하나로 유지하고 새로 시작하지 않는다") {
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
            }
        }

        Given("앞선 다운로드가 끝났다") {
            When("다운로드를 다시 예약한다") {
                Then("작업을 다시 실행한다") {
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
    }) {
    public companion object {
        private fun scheduler(
            work: MusicDownloadWork,
            scope: CoroutineScope,
        ): CoroutineMusicDownloadWorkScheduler =
            CoroutineMusicDownloadWorkScheduler(
                musicDownloadWork = work,
                musicDownloadStateHolder = MusicDownloadStateHolder(),
                scope = scope,
            )
    }
}
