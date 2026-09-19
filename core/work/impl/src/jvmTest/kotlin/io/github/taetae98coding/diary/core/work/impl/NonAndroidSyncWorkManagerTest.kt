@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.core.work.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.work.api.SyncWork
import io.github.taetae98coding.diary.core.work.api.SyncWorkState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid

private val PERIOD = 4.hours

class NonAndroidSyncWorkManagerTest :
    BehaviorSpec({
        Given("실행할 동기화 작업이 준비되어 있다") {
            When("동기화가 요청된다") {
                Then("TC-DATA-SYNC-DOMAIN-016 TC-DATA-SYNC-DOMAIN-044 요청은 곧바로 끝나고 네트워크 연결 확인 없이 동기화 작업이 백그라운드에서 한 번 실행된다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        var executeCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = accountId) } coAnswers { executeCount++ }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )

                        manager.sync(accountId = accountId)

                        executeCount shouldBe 0
                        runCurrent()
                        executeCount shouldBe 1
                    }
                }
            }
        }

        Given("동기화 작업이 진행 중이다") {
            When("동기화가 다시 요청된다") {
                Then("TC-DATA-SYNC-DOMAIN-018 TC-PLACE-HOME-DOMAIN-018 진행 중이던 동기화 작업은 취소되고 새 동기화 작업이 한 번 새로 시작된다") {
                    runTest {
                        val firstAccountId = fixtureMonkey.giveMeOne<Uuid>()
                        val secondAccountId = fixtureMonkey.giveMeOne<Uuid>()
                        var startCount = 0
                        var cancelCount = 0
                        val executionAccountIdList = mutableListOf<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers {
                            executionAccountIdList += firstArg<Uuid>()
                            startCount++
                            try {
                                awaitCancellation()
                            } catch (exception: CancellationException) {
                                cancelCount++
                                throw exception
                            }
                        }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )

                        manager.sync(accountId = firstAccountId)
                        runCurrent()
                        startCount shouldBe 1
                        cancelCount shouldBe 0

                        manager.sync(accountId = secondAccountId)
                        runCurrent()
                        cancelCount shouldBe 1
                        startCount shouldBe 2
                        executionAccountIdList shouldBe listOf(firstAccountId, secondAccountId)
                    }
                }
            }
        }

        Given("실행할 동기화 작업이 준비되어 있다") {
            When("동기화가 요청되어 실행되고 끝난다") {
                Then("TC-SYNC-REFRESH-DOMAIN-002 실행되는 동안 실행 중 상태가 되고 끝나면 남은 작업이 없는 상태가 된다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = accountId) } coAnswers { }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.state.first() shouldBe SyncWorkState.NONE

                        manager.sync(accountId = accountId)
                        manager.state.first() shouldBe SyncWorkState.RUNNING

                        runCurrent()
                        manager.state.first() shouldBe SyncWorkState.NONE
                    }
                }
            }
        }

        Given("동기화 작업이 진행 중이다") {
            When("동기화가 다시 요청된다") {
                Then("TC-SYNC-REFRESH-DOMAIN-009 TC-PLACE-HOME-DOMAIN-018 이어진 작업이 실행되는 동안 실행 중 상태가 유지된다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers { awaitCancellation() }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )

                        manager.sync(accountId = accountId)
                        runCurrent()
                        manager.state.first() shouldBe SyncWorkState.RUNNING

                        manager.sync(accountId = accountId)
                        runCurrent()

                        manager.state.first() shouldBe SyncWorkState.RUNNING
                    }
                }
            }
        }

        Given("동기화 작업이 실패하도록 준비되어 있다") {
            When("실패한 뒤 동기화가 다시 요청된다") {
                Then("동기화 작업이 다시 실행된다") {
                    val accountId = fixtureMonkey.giveMeOne<Uuid>()
                    var executeCount = 0
                    val syncWork = mockk<SyncWork>()
                    coEvery { syncWork.doWork(accountId = accountId) } coAnswers {
                        executeCount++
                        throw IllegalStateException("sync failure")
                    }
                    val scheduler = TestCoroutineScheduler()
                    val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(scheduler) + CoroutineExceptionHandler { _, _ -> })
                    val manager =
                        NonAndroidSyncWorkManager(
                            syncWork = syncWork,
                            scope = scope,
                        )

                    manager.sync(accountId = accountId)
                    scheduler.runCurrent()
                    executeCount shouldBe 1

                    manager.sync(accountId = accountId)
                    scheduler.runCurrent()
                    executeCount shouldBe 2
                }
            }
        }

        Given("주기 동기화가 예약되어 있지 않다") {
            When("인증된 사용자 계정으로 주기 동기화가 예약된다") {
                Then("TC-DATA-SYNC-DOMAIN-056 TC-DATA-SYNC-DOMAIN-060 예약 직후에는 실행되지 않고 주기가 지나면 그 계정으로 한 번 실행된다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        val executionAccountIdList = mutableListOf<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers { executionAccountIdList += firstArg<Uuid>() }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )

                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)

                        runCurrent()
                        executionAccountIdList shouldBe emptyList()

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executionAccountIdList shouldBe listOf(accountId)
                    }
                }
            }

            When("주기 동기화가 예약되어 실행된다") {
                Then("TC-SYNC-REFRESH-FEATURE-011 실행되는 동안에도 진행 표시 대상인 실행 중 상태가 되지 않는다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers { awaitCancellation() }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )

                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)
                        advanceTimeBy(PERIOD)
                        runCurrent()

                        manager.state.first() shouldBe SyncWorkState.NONE
                    }
                }
            }
        }

        Given("인증된 사용자 계정으로 주기 동기화가 예약되어 있다") {
            When("다음 실행까지 주기가 남지 않은 시점에 같은 계정이 다시 확인된다") {
                Then("TC-DATA-SYNC-DOMAIN-057 다음 실행 시각이 뒤로 미뤄지지 않고 예약한 시점으로부터 한 주기 뒤에 한 번 실행된다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        val executionAccountIdList = mutableListOf<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers { executionAccountIdList += firstArg<Uuid>() }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)

                        advanceTimeBy(2.hours)
                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)

                        advanceTimeBy(2.hours)
                        runCurrent()
                        executionAccountIdList shouldBe listOf(accountId)
                    }
                }
            }

            When("다음 실행까지 주기가 남지 않은 시점에 다른 인증된 계정이 확인된다") {
                Then("TC-DATA-SYNC-DOMAIN-058 다음 실행 시각은 유지되고 바뀐 계정으로 실행된다") {
                    runTest {
                        val firstAccountId = fixtureMonkey.giveMeOne<Uuid>()
                        val secondAccountId = fixtureMonkey.giveMeOne<Uuid>()
                        val executionAccountIdList = mutableListOf<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers { executionAccountIdList += firstArg<Uuid>() }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.schedulePeriodicSync(accountId = firstAccountId, period = PERIOD)

                        advanceTimeBy(2.hours)
                        manager.schedulePeriodicSync(accountId = secondAccountId, period = PERIOD)

                        advanceTimeBy(2.hours)
                        runCurrent()
                        executionAccountIdList shouldBe listOf(secondAccountId)
                    }
                }
            }

            When("로그아웃되어 주기 동기화 예약이 해제된다") {
                Then("TC-DATA-SYNC-DOMAIN-059 주기가 지나도 동기화가 실행되지 않는다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        val executionAccountIdList = mutableListOf<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers { executionAccountIdList += firstArg<Uuid>() }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)

                        manager.cancelPeriodicSync()

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executionAccountIdList shouldBe emptyList()
                    }
                }
            }

            When("주기가 돌아와 동기화가 실패한다") {
                Then("TC-DATA-SYNC-DOMAIN-061 예약이 유지되어 다음 주기에 같은 계정으로 다시 실행된다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        val executionAccountIdList = mutableListOf<Uuid>()
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers {
                            executionAccountIdList += firstArg<Uuid>()
                            throw IllegalStateException("sync failure")
                        }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executionAccountIdList shouldBe listOf(accountId)

                        advanceTimeBy(PERIOD)
                        runCurrent()
                        executionAccountIdList shouldBe listOf(accountId, accountId)
                    }
                }
            }
        }

        Given("다른 계기로 시작된 동기화가 진행 중이고 같은 계정으로 주기 동기화가 예약되어 있다") {
            When("주기가 돌아와 주기 동기화가 실행된다") {
                Then("TC-DATA-SYNC-DOMAIN-063 진행 중이던 동기화가 취소되지 않는다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        var startCount = 0
                        var cancelCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers {
                            startCount++
                            try {
                                awaitCancellation()
                            } catch (exception: CancellationException) {
                                cancelCount++
                                throw exception
                            }
                        }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.sync(accountId = accountId)
                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)
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
            When("다른 동기화 계기가 발생해 같은 계정의 동기화가 요청된다") {
                Then("TC-DATA-SYNC-DOMAIN-064 진행 중이던 주기 동기화가 취소되지 않는다") {
                    runTest {
                        val accountId = fixtureMonkey.giveMeOne<Uuid>()
                        var startCount = 0
                        var cancelCount = 0
                        val syncWork = mockk<SyncWork>()
                        coEvery { syncWork.doWork(accountId = any()) } coAnswers {
                            startCount++
                            try {
                                awaitCancellation()
                            } catch (exception: CancellationException) {
                                cancelCount++
                                throw exception
                            }
                        }
                        val manager =
                            NonAndroidSyncWorkManager(
                                syncWork = syncWork,
                                scope = backgroundScope,
                            )
                        manager.schedulePeriodicSync(accountId = accountId, period = PERIOD)
                        advanceTimeBy(PERIOD)
                        runCurrent()
                        startCount shouldBe 1

                        manager.sync(accountId = accountId)
                        runCurrent()

                        startCount shouldBe 2
                        cancelCount shouldBe 0
                    }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
