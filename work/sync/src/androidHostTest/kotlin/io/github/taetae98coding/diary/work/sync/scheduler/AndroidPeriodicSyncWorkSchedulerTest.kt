package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import io.github.taetae98coding.diary.work.sync.work.SyncWork
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidPeriodicSyncWorkSchedulerTest {
    private lateinit var context: Context
    private lateinit var syncWork: SyncWork
    private var executeCount = 0

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        executeCount = 0
        syncWork = mockk<SyncWork>()
        coEvery { syncWork.doWork() } coAnswers { executeCount++ }

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
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(period = PERIOD)

        val workInfo = periodicWorkInfo()
        workInfo.state shouldBe WorkInfo.State.ENQUEUED
        workInfo.periodicityInfo?.repeatIntervalMillis shouldBe PERIOD.inWholeMilliseconds
        workInfo.initialDelayMillis shouldBe PERIOD.inWholeMilliseconds
        executeCount shouldBe 0
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-060 주기가 지나면 동기화가 실행된다`() {
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(period = PERIOD)

        connectPeriodicNetwork()
        elapsePeriod()

        executeCount shouldBe 1
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-062 주기 동기화는 네트워크에 연결되어 있지 않으면 실행하지 않는다`() {
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(period = PERIOD)

        periodicWorkInfo().constraints.requiredNetworkType shouldBe NetworkType.CONNECTED

        elapsePeriod()
        executeCount shouldBe 0

        connectPeriodicNetwork()
        executeCount shouldBe 1
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-057 TC-DATA-SYNC-DOMAIN-058 계정이 다시 확인되어도 다음 주기까지 남은 시간이 유지된다`() {
        val scheduler = AndroidPeriodicSyncWorkScheduler(context = context)
        scheduler.schedule(period = PERIOD)
        val scheduled = periodicWorkInfo()

        ShadowSystemClock.advanceBy(java.time.Duration.ofHours(2))
        scheduler.schedule(period = PERIOD)

        val rescheduled = periodicWorkInfo()
        rescheduled.id shouldBe scheduled.id
        rescheduled.nextScheduleTimeMillis shouldBe scheduled.nextScheduleTimeMillis

        connectPeriodicNetwork()
        elapsePeriod()
        executeCount shouldBe 1
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-059 인증된 계정이 없어지면 주기 동기화 예약을 해제한다`() {
        val scheduler = AndroidPeriodicSyncWorkScheduler(context = context)
        scheduler.schedule(period = PERIOD)

        scheduler.cancel()

        periodicWorkInfoList() shouldBe emptyList()
        executeCount shouldBe 0
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-061 주기 동기화가 실패해도 예약이 유지되어 다음 주기에 다시 시도한다`() {
        coEvery { syncWork.doWork() } coAnswers {
            executeCount++
            throw IllegalStateException("sync failure")
        }
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(period = PERIOD)

        connectPeriodicNetwork()
        elapsePeriod()

        executeCount shouldBe 1
        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED

        connectPeriodicNetwork()
        elapsePeriod()
        executeCount shouldBe 2
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-063 TC-DATA-SYNC-DOMAIN-064 주기 동기화와 다른 계기의 동기화는 서로를 취소하지 않는다`() {
        AndroidPeriodicSyncWorkScheduler(context = context).schedule(period = PERIOD)
        AndroidSyncWorkScheduler(context = context).sync()

        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED
        syncWorkInfo().state shouldBe WorkInfo.State.ENQUEUED

        connectSyncNetwork()
        connectPeriodicNetwork()
        elapsePeriod()

        executeCount shouldBe 2
        periodicWorkInfo().state shouldBe WorkInfo.State.ENQUEUED
    }

    private fun connectSyncNetwork() {
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))

        testDriver.setAllConstraintsMet(syncWorkInfo().id)
    }

    private fun syncWorkInfo(): WorkInfo =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(AndroidSyncWorkScheduler.SYNC_WORK_NAME)
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
        private val PERIOD = 4.hours
    }
}
