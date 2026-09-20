@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.sync.scheduler

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import io.github.taetae98coding.diary.work.sync.work.SyncWork
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class CoroutineSyncWorkSchedulerTest :
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
                            CoroutineSyncWorkScheduler(
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
                            CoroutineSyncWorkScheduler(
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
                            CoroutineSyncWorkScheduler(
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
                            CoroutineSyncWorkScheduler(
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
                        CoroutineSyncWorkScheduler(
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
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
