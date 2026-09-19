package io.github.taetae98coding.diary.core.work.impl

import android.content.Context
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.work.api.SyncWork
import io.github.taetae98coding.diary.core.work.api.SyncWorkState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidSyncWorkManagerTest {
    private lateinit var context: Context
    private lateinit var syncWork: SyncWork
    private lateinit var executionAccountIdList: MutableList<Uuid>

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        executionAccountIdList = mutableListOf()
        syncWork = mockk<SyncWork>()
        coEvery { syncWork.doWork(accountId = any()) } coAnswers { executionAccountIdList += firstArg<Uuid>() }

        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration
                .Builder()
                .setExecutor(SynchronousExecutor())
                .setWorkerFactory(mockSyncWorkerFactory(syncWork))
                .build(),
        )
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-041 네트워크에 연결되지 않았으면 동기화 작업을 실행하지 않고 대기시킨다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()

        AndroidSyncWorkManager(context = context).sync(accountId = accountId)

        val workInfo = enqueuedWorkInfo()
        workInfo.state shouldBe WorkInfo.State.ENQUEUED
        workInfo.constraints.requiredNetworkType shouldBe NetworkType.CONNECTED
        executionAccountIdList shouldBe emptyList()
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-042 네트워크에 연결되면 대기 중인 동기화 작업이 실행된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        AndroidSyncWorkManager(context = context).sync(accountId = accountId)

        connectNetwork()

        enqueuedWorkInfo().state shouldBe WorkInfo.State.SUCCEEDED
        executionAccountIdList shouldBe listOf(accountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-043 연결을 기다리는 동안 여러 계기가 발생해도 연결 후 한 번만 실행된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        val manager = AndroidSyncWorkManager(context = context)
        manager.sync(accountId = accountId)
        manager.sync(accountId = accountId)
        manager.sync(accountId = accountId)

        enqueuedWorkInfoList().size shouldBe 1
        connectNetwork()

        executionAccountIdList shouldBe listOf(accountId)
    }

    @Test
    fun `마지막으로 요청된 계정으로 동기화 작업이 실행된다`() {
        val firstAccountId = fixtureMonkey.giveMeOne<Uuid>()
        val secondAccountId = fixtureMonkey.giveMeOne<Uuid>()
        val manager = AndroidSyncWorkManager(context = context)
        manager.sync(accountId = firstAccountId)
        manager.sync(accountId = secondAccountId)

        connectNetwork()

        executionAccountIdList shouldBe listOf(secondAccountId)
    }

    @Test
    fun `TC-SYNC-REFRESH-DOMAIN-001 네트워크를 기다리며 대기하는 동안에는 실행 중 상태가 아니다`() {
        runBlocking {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val manager = AndroidSyncWorkManager(context = context)

            manager.sync(accountId = accountId)

            manager.state.first() shouldBe SyncWorkState.PENDING
        }
    }

    @Test
    fun `대기 중인 작업이 실행되어 끝나면 남은 작업이 없는 상태가 된다`() {
        runBlocking {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val manager = AndroidSyncWorkManager(context = context)
            manager.sync(accountId = accountId)

            connectNetwork()

            manager.state.first() shouldBe SyncWorkState.NONE
        }
    }

    @Test
    fun `요청한 동기화 작업이 없으면 남은 작업이 없는 상태다`() {
        runBlocking {
            val manager = AndroidSyncWorkManager(context = context)

            manager.state.first() shouldBe SyncWorkState.NONE
        }
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-056 인증된 사용자 계정이 확인되면 4시간 간격의 주기 동기화를 예약한다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()

        AndroidSyncWorkManager(context = context).schedulePeriodicSync(accountId = accountId)

        val workInfo = periodicWorkInfo()
        workInfo.state shouldBe WorkInfo.State.ENQUEUED
        workInfo.periodicityInfo?.repeatIntervalMillis shouldBe SYNC_PERIOD.inWholeMilliseconds
        workInfo.initialDelayMillis shouldBe SYNC_PERIOD.inWholeMilliseconds
        executionAccountIdList shouldBe emptyList()
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-060 4시간이 지나면 지정된 계정의 동기화가 실행된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        AndroidSyncWorkManager(context = context).schedulePeriodicSync(accountId = accountId)

        connectPeriodicNetwork()
        elapsePeriod()

        executionAccountIdList shouldBe listOf(accountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-062 주기 동기화는 네트워크에 연결되어 있지 않으면 실행하지 않는다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        AndroidSyncWorkManager(context = context).schedulePeriodicSync(accountId = accountId)

        periodicWorkInfo().constraints.requiredNetworkType shouldBe NetworkType.CONNECTED

        elapsePeriod()
        executionAccountIdList shouldBe emptyList()

        connectPeriodicNetwork()
        executionAccountIdList shouldBe listOf(accountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-057 같은 계정이 다시 확인되어도 다음 주기까지 남은 시간이 유지된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        val manager = AndroidSyncWorkManager(context = context)
        manager.schedulePeriodicSync(accountId = accountId)
        val scheduled = periodicWorkInfo()

        ShadowSystemClock.advanceBy(Duration.ofHours(2))
        manager.schedulePeriodicSync(accountId = accountId)

        val rescheduled = periodicWorkInfo()
        rescheduled.id shouldBe scheduled.id
        rescheduled.nextScheduleTimeMillis shouldBe scheduled.nextScheduleTimeMillis
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-058 확인된 계정이 바뀌면 주기 동기화의 대상 계정만 바뀐다`() {
        val firstAccountId = fixtureMonkey.giveMeOne<Uuid>()
        val secondAccountId = fixtureMonkey.giveMeOne<Uuid>()
        val manager = AndroidSyncWorkManager(context = context)
        manager.schedulePeriodicSync(accountId = firstAccountId)
        val scheduled = periodicWorkInfo()

        ShadowSystemClock.advanceBy(Duration.ofHours(2))
        manager.schedulePeriodicSync(accountId = secondAccountId)

        val rescheduled = periodicWorkInfo()
        rescheduled.id shouldBe scheduled.id
        rescheduled.nextScheduleTimeMillis shouldBe scheduled.nextScheduleTimeMillis

        connectPeriodicNetwork()
        elapsePeriod()
        executionAccountIdList shouldBe listOf(secondAccountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-059 인증된 계정이 없어지면 주기 동기화 예약을 해제한다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        val manager = AndroidSyncWorkManager(context = context)
        manager.schedulePeriodicSync(accountId = accountId)

        manager.cancelPeriodicSync()

        periodicWorkInfoList() shouldBe emptyList()
        executionAccountIdList shouldBe emptyList()
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-061 주기 동기화가 실패해도 예약이 유지되어 다음 주기에 다시 시도한다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        coEvery { syncWork.doWork(accountId = any()) } coAnswers {
            executionAccountIdList += firstArg<Uuid>()
            throw IllegalStateException("sync failure")
        }
        AndroidSyncWorkManager(context = context).schedulePeriodicSync(accountId = accountId)

        connectPeriodicNetwork()
        elapsePeriod()

        executionAccountIdList shouldBe listOf(accountId)
        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED

        connectPeriodicNetwork()
        elapsePeriod()
        executionAccountIdList shouldBe listOf(accountId, accountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-063 TC-DATA-SYNC-DOMAIN-064 주기 동기화와 다른 계기의 동기화는 서로를 취소하지 않는다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        val manager = AndroidSyncWorkManager(context = context)

        manager.schedulePeriodicSync(accountId = accountId)
        manager.sync(accountId = accountId)

        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED
        enqueuedWorkInfo().state shouldBe WorkInfo.State.ENQUEUED

        connectNetwork()
        connectPeriodicNetwork()
        elapsePeriod()

        executionAccountIdList shouldBe listOf(accountId, accountId)
        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED
    }

    private fun connectNetwork() {
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))

        enqueuedWorkInfoList().forEach { workInfo -> testDriver.setAllConstraintsMet(workInfo.id) }
    }

    private fun enqueuedWorkInfo(): WorkInfo = enqueuedWorkInfoList().single()

    private fun enqueuedWorkInfoList(): List<WorkInfo> =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(AndroidSyncWorkManager.SYNC_WORK_NAME)
            .get()
            .filterNot { workInfo -> workInfo.state == WorkInfo.State.CANCELLED }

    private fun connectPeriodicNetwork() {
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))

        testDriver.setAllConstraintsMet(periodicWorkInfo().id)
    }

    private fun elapsePeriod() {
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))
        val id = periodicWorkInfo().id

        testDriver.setInitialDelayMet(id)
        testDriver.setPeriodDelayMet(id)
    }

    private fun periodicWorkInfo(): WorkInfo = periodicWorkInfoList().single()

    private fun periodicWorkInfoList(): List<WorkInfo> =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(AndroidSyncWorkManager.PERIODIC_SYNC_WORK_NAME)
            .get()
            .filterNot { workInfo -> workInfo.state == WorkInfo.State.CANCELLED }

    companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}

private fun mockSyncWorkerFactory(syncWork: SyncWork): WorkerFactory =
    mockk {
        every { createWorker(any(), any(), any()) } answers {
            SyncWorker(
                context = firstArg<Context>(),
                parameters = thirdArg<WorkerParameters>(),
                syncWork = syncWork,
            )
        }
        every { createWorkerWithDefaultFallback(any(), any(), any()) } answers {
            SyncWorker(
                context = firstArg<Context>(),
                parameters = thirdArg<WorkerParameters>(),
                syncWork = syncWork,
            )
        }
    }
