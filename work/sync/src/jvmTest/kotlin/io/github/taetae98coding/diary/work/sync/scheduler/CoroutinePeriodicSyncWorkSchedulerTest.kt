@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.hours

private val PERIOD = 4.hours
private val SYNC_DURATION = 1.hours

class CoroutinePeriodicSyncWorkSchedulerTest :
    BehaviorSpec({
        Given("주기 동기화가 예약되어 있지 않다") {
            When("인증된 사용자 계정으로 주기 동기화가 예약된다") {
                Then("TC-d271b7b4-fb16-488a-a26d-a93e0462cc67 TC-16de5077-3cce-4a3f-a51d-55d6f357de46 예약 직후에는 실행되지 않고 주기가 지나면 한 번 실행된다") {
                    runTest {
                        var executeCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers { executeCount++ }
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )

                        scheduler.schedule(period = PERIOD)

                        runCurrent()
                        executeCount shouldBe 0

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executeCount shouldBe 1
                    }
                }
            }

            When("주기 동기화가 예약되어 실행된다") {
                Then("TC-16116bd8-e994-44d9-8a67-4d3920488939 실행되는 동안에도 진행 표시 대상인 실행 중 상태가 되지 않는다") {
                    runTest {
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers { awaitCancellation() }
                        val manager =
                            CoroutineSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )

                        scheduler.schedule(period = PERIOD)
                        advanceTimeBy(PERIOD)
                        runCurrent()

                        manager.state.first() shouldBe SyncWorkState.NONE
                    }
                }
            }
        }

        Given("인증된 사용자 계정으로 주기 동기화가 예약되어 있다") {
            When("다음 실행까지 주기가 남지 않은 시점에 계정이 다시 확인된다") {
                Then("TC-6469282a-59e0-4ac6-8976-a566838d6669 TC-c52f665c-e8d8-45a8-a3aa-28ed08b2e5cf 다음 실행 시각이 뒤로 미뤄지지 않고 예약한 시점으로부터 한 주기 뒤에 한 번 실행된다") {
                    runTest {
                        var executeCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers { executeCount++ }
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        scheduler.schedule(period = PERIOD)

                        advanceTimeBy(2.hours)
                        scheduler.schedule(period = PERIOD)

                        advanceTimeBy(2.hours)
                        runCurrent()
                        executeCount shouldBe 1
                    }
                }
            }

            When("로그아웃되어 주기 동기화 예약이 해제된다") {
                Then("TC-cc07f234-2c6a-4021-a650-4a0161718819 주기가 지나도 동기화가 실행되지 않는다") {
                    runTest {
                        var executeCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers { executeCount++ }
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        scheduler.schedule(period = PERIOD)

                        scheduler.cancel()

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executeCount shouldBe 0
                    }
                }
            }

            When("주기가 돌아와 동기화가 실패한다") {
                Then("TC-d44c3656-3097-4c89-a4a3-9afb6c702f18 예약이 유지되어 다음 주기에 다시 실행된다") {
                    runTest {
                        var executeCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers {
                            executeCount++
                            throw IllegalStateException("sync failure")
                        }
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        scheduler.schedule(period = PERIOD)

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executeCount shouldBe 1

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executeCount shouldBe 2
                    }
                }
            }
        }

        Given("데스크톱 앱이나 웹에서 주기 동기화가 예약된 뒤 앱이 종료되었다") {
            When("앱을 다시 실행해 같은 계정으로 주기 동기화가 예약된다") {
                Then("TC-cb7a4801-1243-480e-b2c0-e7a19cfd6024 종료 전에 흐른 시간과 무관하게 다시 예약한 시점으로부터 한 주기 뒤에 처음 실행된다") {
                    runTest {
                        var executeCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers { executeCount++ }
                        // 앱 실행 하나를 scope 하나로 보고, 종료는 그 scope를 취소하는 것으로 나타낸다.
                        val firstRun = CoroutineScope(backgroundScope.coroutineContext + Job(backgroundScope.coroutineContext[Job]))
                        CoroutinePeriodicSyncWorkScheduler(syncWork = syncWork, scope = firstRun).schedule(period = PERIOD)
                        advanceTimeBy(3.hours)
                        firstRun.cancel()

                        CoroutinePeriodicSyncWorkScheduler(syncWork = syncWork, scope = backgroundScope).schedule(period = PERIOD)

                        advanceTimeBy(1.hours)
                        runCurrent()
                        executeCount shouldBe 0

                        advanceTimeBy(3.hours)
                        runCurrent()
                        executeCount shouldBe 1
                    }
                }
            }
        }

        Given("동기화가 끝나는 데 시간이 걸리는 주기 동기화가 예약되어 있다") {
            When("주기가 돌아와 주기 동기화가 실행되고 끝난다") {
                Then("TC-bf08b1cb-ac1b-4394-968d-45a34be0cc2c 끝난 시점부터 다음 주기를 센다") {
                    runTest {
                        var executeCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers {
                            executeCount++
                            delay(SYNC_DURATION)
                        }
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        scheduler.schedule(period = PERIOD)
                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executeCount shouldBe 1

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executeCount shouldBe 1

                        advanceTimeBy(SYNC_DURATION)
                        runCurrent()
                        executeCount shouldBe 2
                    }
                }
            }
        }

        Given("다른 계기로 시작된 동기화가 진행 중이고 주기 동기화가 예약되어 있다") {
            When("주기가 돌아와 주기 동기화가 실행된다") {
                Then("TC-82abafa0-6d03-47ef-8573-9b4654fd045f 진행 중이던 동기화가 취소되지 않는다") {
                    runTest {
                        var startCount = 0
                        var cancelCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers {
                            startCount++
                            try {
                                awaitCancellation()
                            } catch (exception: CancellationException) {
                                cancelCount++
                                throw exception
                            }
                        }
                        val manager =
                            CoroutineSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.sync()
                        scheduler.schedule(period = PERIOD)
                        runCurrent()
                        startCount shouldBe 1

                        advanceTimeBy(PERIOD)
                        runCurrent()

                        startCount shouldBe 2
                        cancelCount shouldBe 0
                    }
                }
            }
        }

        Given("주기 동기화가 실행되어 진행 중이다") {
            When("다른 동기화 계기가 발생해 동기화가 요청된다") {
                Then("TC-b31869f4-8236-4dd1-8b15-f1f4c1971dec 진행 중이던 주기 동기화가 취소되지 않는다") {
                    runTest {
                        var startCount = 0
                        var cancelCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork() } coAnswers {
                            startCount++
                            try {
                                awaitCancellation()
                            } catch (exception: CancellationException) {
                                cancelCount++
                                throw exception
                            }
                        }
                        val manager =
                            CoroutineSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        val scheduler =
                            CoroutinePeriodicSyncWorkScheduler(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        scheduler.schedule(period = PERIOD)
                        advanceTimeBy(PERIOD)
                        runCurrent()
                        startCount shouldBe 1

                        manager.sync()
                        runCurrent()

                        startCount shouldBe 2
                        cancelCount shouldBe 0
                    }
                }
            }
        }
    })
