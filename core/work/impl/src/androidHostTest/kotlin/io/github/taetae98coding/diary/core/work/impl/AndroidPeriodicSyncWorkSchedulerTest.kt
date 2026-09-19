package io.github.taetae98coding.diary.core.work.impl

import android.content.Context
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.work.api.SyncWork
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidPeriodicSyncWorkSchedulerTest {
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
    fun `TC-DATA-SYNC-DOMAIN-056 인증된 사용자 계정이 확인되면 주어진 주기의 동기화를 예약한다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()

        AndroidPeriodicSyncWorkScheduler(context = context).schedule(accountId = accountId, period = PERIOD)

        val workInfo = periodicWorkInfo()
        workInfo.state shouldBe WorkInfo.State.ENQUEUED
        workInfo.periodicityInfo?.repeatIntervalMillis shouldBe PERIOD.inWholeMilliseconds
        workInfo.initialDelayMillis shouldBe PERIOD.inWholeMilliseconds
        executionAccountIdList shouldBe emptyList()
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-060 주기가 지나면 지정된 계정의 동기화가 실행된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(accountId = accountId, period = PERIOD)

        connectPeriodicNetwork()
        elapsePeriod()

        executionAccountIdList shouldBe listOf(accountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-062 주기 동기화는 네트워크에 연결되어 있지 않으면 실행하지 않는다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(accountId = accountId, period = PERIOD)

        periodicWorkInfo().constraints.requiredNetworkType shouldBe NetworkType.CONNECTED

        elapsePeriod()
        executionAccountIdList shouldBe emptyList()

        connectPeriodicNetwork()
        executionAccountIdList shouldBe listOf(accountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-057 같은 계정이 다시 확인되어도 다음 주기까지 남은 시간이 유지된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        val scheduler = AndroidPeriodicSyncWorkScheduler(context = context)
        scheduler.schedule(accountId = accountId, period = PERIOD)
        val scheduled = periodicWorkInfo()

        ShadowSystemClock.advanceBy(java.time.Duration.ofHours(2))
        scheduler.schedule(accountId = accountId, period = PERIOD)

        val rescheduled = periodicWorkInfo()
        rescheduled.id shouldBe scheduled.id
        rescheduled.nextScheduleTimeMillis shouldBe scheduled.nextScheduleTimeMillis
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-058 확인된 계정이 바뀌면 주기 동기화의 대상 계정만 바뀐다`() {
        val firstAccountId = fixtureMonkey.giveMeOne<Uuid>()
        val secondAccountId = fixtureMonkey.giveMeOne<Uuid>()
        val scheduler = AndroidPeriodicSyncWorkScheduler(context = context)
        scheduler.schedule(accountId = firstAccountId, period = PERIOD)
        val scheduled = periodicWorkInfo()

        ShadowSystemClock.advanceBy(java.time.Duration.ofHours(2))
        scheduler.schedule(accountId = secondAccountId, period = PERIOD)

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
        val scheduler = AndroidPeriodicSyncWorkScheduler(context = context)
        scheduler.schedule(accountId = accountId, period = PERIOD)

        scheduler.cancel()

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
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(accountId = accountId, period = PERIOD)

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

        AndroidPeriodicSyncWorkScheduler(context = context).schedule(accountId = accountId, period = PERIOD)
        AndroidSyncWorkManager(context = context).sync(accountId = accountId)

        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED
        syncWorkInfo().state shouldBe WorkInfo.State.ENQUEUED

        connectSyncNetwork()
        connectPeriodicNetwork()
        elapsePeriod()

        executionAccountIdList shouldBe listOf(accountId, accountId)
        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED
    }

    private fun connectSyncNetwork() {
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))

        testDriver.setAllConstraintsMet(syncWorkInfo().id)
    }

    private fun syncWorkInfo(): WorkInfo =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(AndroidSyncWorkManager.SYNC_WORK_NAME)
            .get()
            .filterNot { workInfo -> workInfo.state == WorkInfo.State.CANCELLED }
            .single()

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
            .getWorkInfosForUniqueWork(AndroidPeriodicSyncWorkScheduler.PERIODIC_SYNC_WORK_NAME)
            .get()
            .filterNot { workInfo -> workInfo.state == WorkInfo.State.CANCELLED }

    companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private val PERIOD = 4.hours
    }
}
