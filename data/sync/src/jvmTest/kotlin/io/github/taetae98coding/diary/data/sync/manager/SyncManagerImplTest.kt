@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.data.sync.manager

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.work.api.SyncWorkManager
import io.github.taetae98coding.diary.core.work.api.SyncWorkState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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
import kotlin.uuid.Uuid

class SyncManagerImplTest :
    FunSpec({
        test("동기화 요청의 계정 식별자를 백그라운드 작업에 전달한다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncWorkManager = syncWorkManager(workState = workState)
                val syncManager = syncManager(syncWorkManager = syncWorkManager, scope = backgroundScope)

                syncManager.requestSync(accountId = accountId, reportsProgress = false)

                verify(exactly = 1) { syncWorkManager.sync(accountId = accountId) }
            }
        }

        test("진행을 보고하는 동기화 요청의 계정 식별자를 백그라운드 작업에 전달한다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncWorkManager = syncWorkManager(workState = workState)
                val syncManager = syncManager(syncWorkManager = syncWorkManager, scope = backgroundScope)

                syncManager.requestSync(accountId = accountId, reportsProgress = true)

                verify(exactly = 1) { syncWorkManager.sync(accountId = accountId) }
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-002 진행 보고를 요청한 동기화가 실행되면 진행 표시 대상이 된다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.requestSync(accountId = accountId, reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-004 변경으로 시작된 동기화가 실행되어도 진행 표시 대상이 아니다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.requestSync(accountId = accountId, reportsProgress = false)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-005 동기화가 끝나면 진행 표시 대상에서 해제된다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(accountId = accountId, reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                workState.value = SyncWorkState.NONE
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-001 요청한 동기화가 실행되지 않고 기다리는 동안에는 진행 표시 대상이 아니다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.requestSync(accountId = accountId, reportsProgress = true)
                workState.value = SyncWorkState.PENDING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-002 기다리던 작업이 실행되기 시작하면 진행 표시 대상이 된다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(accountId = accountId, reportsProgress = true)
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
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(accountId = accountId, reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.requestSync(accountId = accountId, reportsProgress = false)
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-006 이어진 동기화가 끝난 뒤의 변경 계기는 진행 표시 대상이 아니다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(accountId = accountId, reportsProgress = true)
                workState.value = SyncWorkState.RUNNING
                runCurrent()
                workState.value = SyncWorkState.NONE
                runCurrent()

                syncManager.requestSync(accountId = accountId, reportsProgress = false)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }

        test("TC-SYNC-REFRESH-DOMAIN-010 관찰이 끊긴 뒤 다시 관찰해도 실행 중이면 진행 표시 대상이다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.requestSync(accountId = accountId, reportsProgress = true)
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
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                // 앱을 다시 실행해 표시 대상 여부가 초기화된 상태에서 이전 실행의 작업이 계속 실행되고 있다.
                val workState = MutableStateFlow(SyncWorkState.RUNNING)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()
                syncManager.isProgressReported.value shouldBe false

                syncManager.requestSync(accountId = accountId, reportsProgress = true)
                runCurrent()

                syncManager.isProgressReported.value shouldBe true
            }
        }

        test("TC-DATA-SYNC-DOMAIN-056 주기 동기화 예약의 계정 식별자를 백그라운드 작업에 전달한다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncWorkManager = syncWorkManager(workState = workState)
                val syncManager = syncManager(syncWorkManager = syncWorkManager, scope = backgroundScope)

                syncManager.schedulePeriodicSync(accountId = accountId)

                verify(exactly = 1) { syncWorkManager.schedulePeriodicSync(accountId = accountId) }
            }
        }

        test("TC-DATA-SYNC-DOMAIN-059 주기 동기화 예약 해제를 백그라운드 작업에 전달한다") {
            runTest {
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncWorkManager = syncWorkManager(workState = workState)
                val syncManager = syncManager(syncWorkManager = syncWorkManager, scope = backgroundScope)

                syncManager.cancelPeriodicSync()

                verify(exactly = 1) { syncWorkManager.cancelPeriodicSync() }
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-011 주기 동기화만 예약되어 실행되면 진행 표시 대상이 되지 않는다") {
            runTest {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val workState = MutableStateFlow(SyncWorkState.NONE)
                val syncManager = syncManager(syncWorkManager = syncWorkManager(workState = workState), scope = backgroundScope)
                runCurrent()

                syncManager.schedulePeriodicSync(accountId = accountId)
                workState.value = SyncWorkState.RUNNING
                runCurrent()

                syncManager.isProgressReported.value shouldBe false
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun syncWorkManager(workState: MutableStateFlow<SyncWorkState>): SyncWorkManager =
            mockk<SyncWorkManager>().also { manager ->
                every { manager.state } returns workState
                justRun { manager.sync(accountId = any()) }
                justRun { manager.schedulePeriodicSync(accountId = any()) }
                justRun { manager.cancelPeriodicSync() }
            }

        private fun syncManager(
            syncWorkManager: SyncWorkManager,
            scope: CoroutineScope,
        ): SyncManagerImpl =
            SyncManagerImpl(
                syncWorkManager = syncWorkManager,
                scope = scope,
            )
    }
}
