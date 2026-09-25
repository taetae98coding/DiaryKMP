@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.sync.manager

import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.hours

class SyncManagerImplTest :
    FunSpec({
        test("진행 보고 여부와 무관하게 동기화 요청을 백그라운드 작업에 전달한다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncWorkScheduler = syncWorkScheduler(workState = workState)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler, scope = backgroundScope)

                syncManager.requestSync(reportsProgress = false)
                syncManager.requestSync(reportsProgress = true)

                verify(exactly = 2) { syncWorkScheduler.sync() }
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-002 진행 보고를 요청한 동기화가 실행되면 진행 표시 대상이 된다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-004 변경으로 시작된 동기화가 실행되어도 진행 표시 대상이 아니다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.requestSync(reportsProgress = false)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-005 동기화가 끝나면 진행 표시 대상에서 해제된다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                workState.value = SyncWorkState.NONE
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-001 요청한 동기화가 실행되지 않고 기다리는 동안에는 진행 표시 대상이 아니다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.PENDING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-002 기다리던 작업이 실행되기 시작하면 진행 표시 대상이 된다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.PENDING
                runCurrent()
                syncManager.isProgressReported.value shouldBe false

                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-005 진행 표시 중 변경으로 새 동기화가 이어져도 진행 표시가 유지된다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.requestSync(reportsProgress = false)
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-006 이어진 동기화가 끝난 뒤의 변경 계기는 진행 표시 대상이 아니다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()
                workState.value = SyncWorkState.NONE
                runCurrent()

                syncManager.requestSync(reportsProgress = false)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-010 관찰이 끊긴 뒤 다시 관찰해도 실행 중이면 진행 표시 대상이다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                val job = launch { syncManager.isProgressReported.collect { } }
                runCurrent()
                job.cancelAndJoin()
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-011 TC-SYNC-REFRESH-DOMAIN-012 이어서 실행 중인 작업은 계정이 확인될 때부터 진행 표시 대상이 된다") {
            runTest {
                // 앱을 다시 실행해 표시 대상 여부가 초기화된 상태에서 이전 실행의 작업이 계속 실행되고 있다.
                val workState = MutableStateFlow(SyncWorkState.RUNNING)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.isProgressReported.value shouldBe false

                syncManager.requestSync(reportsProgress = true)
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-013 한 화면에서 당겨 시작한 동기화가 실행 중이면 옮겨 간 다른 화면에도 진행이 표시된다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                val pulledScreenProgressList = mutableListOf<Boolean>()
                val pulledScreen = launch { syncManager.isProgressReported.collect { value -> pulledScreenProgressList += value } }
                runCurrent()
                syncManager.requestSync(reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()
                pulledScreen.cancelAndJoin()

                val movedScreenProgressList = mutableListOf<Boolean>()
                val movedScreen = launch { syncManager.isProgressReported.collect { value -> movedScreenProgressList += value } }
                runCurrent()
                movedScreen.cancelAndJoin()

                pulledScreenProgressList.last() shouldBe true
                movedScreenProgressList shouldBe listOf(true)
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-014 다른 화면에 다녀오면 돌아온 시점의 실행 여부로 진행 표시를 다시 정한다") {
            val caseList =
                listOf(
                    SyncWorkState.RUNNING to true,
                    SyncWorkState.NONE to false,
                )

            caseList.forEach { (returnedWorkState, isProgressReported) ->
                runTest {
                    val workState = MutableStateFlow(SyncWorkState.NONE)
                    val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                    val screen = launch { syncManager.isProgressReported.collect { } }
                    runCurrent()
                    syncManager.requestSync(reportsProgress = true)
                    workState.value = SyncWorkState.RUNNING
                    runCurrent()
                    screen.cancelAndJoin()

                    workState.value = returnedWorkState
                    runCurrent()
                    val returnedProgressList = mutableListOf<Boolean>()
                    val returnedScreen = launch { syncManager.isProgressReported.collect { value -> returnedProgressList += value } }
                    runCurrent()
                    returnedScreen.cancelAndJoin()

                    returnedProgressList shouldBe listOf(isProgressReported)
                }
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-014 다른 화면에 다녀온 사이 표시 대상이 아닌 동기화만 실행 중이면 진행을 표시하지 않는다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.requestSync(reportsProgress = false)
                workState.value = SyncWorkState.RUNNING
                runCurrent()
                val returnedProgressList = mutableListOf<Boolean>()
                val returnedScreen = launch { syncManager.isProgressReported.collect { value -> returnedProgressList += value } }
                runCurrent()
                returnedScreen.cancelAndJoin()

                returnedProgressList shouldBe listOf(false)
            }
        }

        test("TC-DATA-SYNC-DOMAIN-056 주기 동기화 예약의 주기를 백그라운드 작업에 전달한다") {
            runTest {
                val period = 4.hours
                val periodicSyncWorkScheduler = periodicSyncWorkScheduler()
                val syncManager = syncManager(periodicSyncWorkScheduler = periodicSyncWorkScheduler, scope = backgroundScope)

                syncManager.schedulePeriodicSync(period = period)

                verify(exactly = 1) { periodicSyncWorkScheduler.schedule(period = period) }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-059 주기 동기화 예약 해제를 백그라운드 작업에 전달한다") {
            runTest {
                val periodicSyncWorkScheduler = periodicSyncWorkScheduler()
                val syncManager = syncManager(periodicSyncWorkScheduler = periodicSyncWorkScheduler, scope = backgroundScope)

                syncManager.cancelPeriodicSync()

                verify(exactly = 1) { periodicSyncWorkScheduler.cancel() }
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-011 주기 동기화만 예약되어 실행되면 진행 표시 대상이 되지 않는다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkScheduler = syncWorkScheduler(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.schedulePeriodicSync(period = 4.hours)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }
    }) {
    public companion object {
        private fun syncWorkScheduler(workState: MutableStateFlow<SyncWorkState>): SyncWorkScheduler =
            mockk<SyncWorkScheduler>().also { manager ->
                every { manager.state } returns workState
                justRun { manager.sync() }
            }

        private fun periodicSyncWorkScheduler(): PeriodicSyncWorkScheduler =
            mockk<PeriodicSyncWorkScheduler>().also { scheduler ->
                justRun { scheduler.schedule(period = any()) }
                justRun { scheduler.cancel() }
            }

        private fun syncManager(
            syncWorkScheduler: SyncWorkScheduler = syncWorkScheduler(workState = MutableStateFlow(SyncWorkState.NONE)),
            periodicSyncWorkScheduler: PeriodicSyncWorkScheduler = periodicSyncWorkScheduler(),
            scope: CoroutineScope,
        ): SyncManagerImpl =
            SyncManagerImpl(
                syncWorkScheduler = syncWorkScheduler,
                periodicSyncWorkScheduler = periodicSyncWorkScheduler,
                scope = scope,
            )
    }
}
