package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidSyncWorkSchedulerTest {
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
    fun `TC-29e99d7e-20c8-4721-8f6a-e3b249fb2ee3 네트워크에 연결되지 않았으면 동기화 작업을 실행하지 않고 대기시킨다`() {
        AndroidSyncWorkScheduler(context = context).sync()

        val workInfo = enqueuedWorkInfo()
        workInfo.state shouldBe WorkInfo.State.ENQUEUED
        workInfo.constraints.requiredNetworkType shouldBe NetworkType.CONNECTED
        executeCount shouldBe 0
    }

    @Test
    fun `TC-91a07888-6299-4581-b187-b769d27e6d1a 네트워크에 연결되면 대기 중인 동기화 작업이 실행된다`() {
        AndroidSyncWorkScheduler(context = context).sync()

        connectNetwork()

        enqueuedWorkInfo().state shouldBe WorkInfo.State.SUCCEEDED
        executeCount shouldBe 1
    }

    @Test
    fun `TC-965bab9d-893c-4a50-983d-cf3e1119827f 연결을 기다리는 동안 여러 계기가 발생해도 연결 후 한 번만 실행된다`() {
        val manager = AndroidSyncWorkScheduler(context = context)
        manager.sync()
        manager.sync()
        manager.sync()

        enqueuedWorkInfoList().size shouldBe 1
        connectNetwork()

        executeCount shouldBe 1
    }

    @Test
    fun `마지막으로 요청된 계정으로 동기화 작업이 실행된다`() {
        val manager = AndroidSyncWorkScheduler(context = context)
        manager.sync()
        manager.sync()

        connectNetwork()

        executeCount shouldBe 1
    }

    @Test
    fun `TC-d3c86eec-fa12-4227-9773-521cf1a7800b 네트워크를 기다리며 대기하는 동안에는 실행 중 상태가 아니다`() {
        runBlocking {
            val manager = AndroidSyncWorkScheduler(context = context)

            manager.sync()

            manager.state.first() shouldBe SyncWorkState.PENDING
        }
    }

    @Test
    fun `TC-19d9feda-6635-4b91-a41f-448cb4703f92 대기 중인 작업이 실행되어 성공으로 끝나면 남은 작업이 없는 상태가 된다`() {
        runBlocking {
            val manager = AndroidSyncWorkScheduler(context = context)
            manager.sync()

            connectNetwork()

            manager.state.first() shouldBe SyncWorkState.NONE
        }
    }

    @Test
    fun `TC-19d9feda-6635-4b91-a41f-448cb4703f92 실행 중인 작업이 실패로 끝나도 남은 작업이 없는 상태가 된다`() {
        runBlocking {
            coEvery { syncWork.doWork() } throws IllegalStateException("sync failure")
            val manager = AndroidSyncWorkScheduler(context = context)
            manager.sync()

            connectNetwork()

            manager.state.first() shouldBe SyncWorkState.NONE
        }
    }

    @Test
    fun `TC-904c3de1-9a96-4f74-9e76-74ebeaedfe00 TC-1f820570-271d-47fe-afe1-2e6d8126d8dd 실행 중에 다시 요청해도 남은 작업이 없는 상태를 거치지 않고 이어진 작업이 끝나야 끝난다`() {
        runBlocking {
            val firstWorkGate = CompletableDeferred<Unit>()
            coEvery { syncWork.doWork() } coAnswers {
                executeCount++
                if (executeCount == 1) firstWorkGate.await()
            }
            val manager = AndroidSyncWorkScheduler(context = context)
            manager.sync()
            connectNetwork()
            withTimeout(TIMEOUT_MILLIS) { manager.state.first { state -> state == SyncWorkState.RUNNING } }

            manager.sync()

            manager.state.first() shouldBe SyncWorkState.PENDING
            connectNetwork()
            withTimeout(TIMEOUT_MILLIS) { manager.state.first { state -> state == SyncWorkState.NONE } }
            executeCount shouldBe 2
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

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
