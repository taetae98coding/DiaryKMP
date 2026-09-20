@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.hours

private val PERIOD = 4.hours

class CoroutinePeriodicSyncWorkSchedulerTest :
    BehaviorSpec({
        Given("주기 동기화가 예약되어 있지 않다") {
            When("인증된 사용자 계정으로 주기 동기화가 예약된다") {
                Then("TC-DATA-SYNC-DOMAIN-056 TC-DATA-SYNC-DOMAIN-060 예약 직후에는 실행되지 않고 주기가 지나면 한 번 실행된다") {
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
                Then("TC-SYNC-REFRESH-FEATURE-011 실행되는 동안에도 진행 표시 대상인 실행 중 상태가 되지 않는다") {
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
                Then("TC-DATA-SYNC-DOMAIN-057 TC-DATA-SYNC-DOMAIN-058 다음 실행 시각이 뒤로 미뤄지지 않고 예약한 시점으로부터 한 주기 뒤에 한 번 실행된다") {
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
                Then("TC-DATA-SYNC-DOMAIN-059 주기가 지나도 동기화가 실행되지 않는다") {
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
                Then("TC-DATA-SYNC-DOMAIN-061 예약이 유지되어 다음 주기에 다시 실행된다") {
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

        Given("다른 계기로 시작된 동기화가 진행 중이고 주기 동기화가 예약되어 있다") {
            When("주기가 돌아와 주기 동기화가 실행된다") {
                Then("TC-DATA-SYNC-DOMAIN-063 진행 중이던 동기화가 취소되지 않는다") {
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
                Then("TC-DATA-SYNC-DOMAIN-064 진행 중이던 주기 동기화가 취소되지 않는다") {
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
