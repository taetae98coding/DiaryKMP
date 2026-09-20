package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidSyncWorkSchedulerTest {
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

        AndroidSyncWorkScheduler(context = context).sync(accountId = accountId)

        val workInfo = enqueuedWorkInfo()
        workInfo.state shouldBe WorkInfo.State.ENQUEUED
        workInfo.constraints.requiredNetworkType shouldBe NetworkType.CONNECTED
        executionAccountIdList shouldBe emptyList()
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-042 네트워크에 연결되면 대기 중인 동기화 작업이 실행된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        AndroidSyncWorkScheduler(context = context).sync(accountId = accountId)

        connectNetwork()

        enqueuedWorkInfo().state shouldBe WorkInfo.State.SUCCEEDED
        executionAccountIdList shouldBe listOf(accountId)
    }

    @Test
    fun `TC-DATA-SYNC-DOMAIN-043 연결을 기다리는 동안 여러 계기가 발생해도 연결 후 한 번만 실행된다`() {
        val accountId = fixtureMonkey.giveMeOne<Uuid>()
        val manager = AndroidSyncWorkScheduler(context = context)
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
        val manager = AndroidSyncWorkScheduler(context = context)
        manager.sync(accountId = firstAccountId)
        manager.sync(accountId = secondAccountId)

        connectNetwork()

        executionAccountIdList shouldBe listOf(secondAccountId)
    }

    @Test
    fun `TC-SYNC-REFRESH-DOMAIN-001 네트워크를 기다리며 대기하는 동안에는 실행 중 상태가 아니다`() {
        runBlocking {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val manager = AndroidSyncWorkScheduler(context = context)

            manager.sync(accountId = accountId)

            manager.state.first() shouldBe SyncWorkState.PENDING
        }
    }

    @Test
    fun `대기 중인 작업이 실행되어 끝나면 남은 작업이 없는 상태가 된다`() {
        runBlocking {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val manager = AndroidSyncWorkScheduler(context = context)
            manager.sync(accountId = accountId)

            connectNetwork()

            manager.state.first() shouldBe SyncWorkState.NONE
        }
    }

    @Test
    fun `요청한 동기화 작업이 없으면 남은 작업이 없는 상태다`() {
        runBlocking {
            val manager = AndroidSyncWorkScheduler(context = context)

            manager.state.first() shouldBe SyncWorkState.NONE
        }
    }

    private fun connectNetwork() {
        val testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))

        enqueuedWorkInfoList().forEach { workInfo -> testDriver.setAllConstraintsMet(workInfo.id) }
    }

    private fun enqueuedWorkInfo(): WorkInfo = enqueuedWorkInfoList().single()

    private fun enqueuedWorkInfoList(): List<WorkInfo> =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(AndroidSyncWorkScheduler.SYNC_WORK_NAME)
            .get()
            .filterNot { workInfo -> workInfo.state == WorkInfo.State.CANCELLED }

    companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
