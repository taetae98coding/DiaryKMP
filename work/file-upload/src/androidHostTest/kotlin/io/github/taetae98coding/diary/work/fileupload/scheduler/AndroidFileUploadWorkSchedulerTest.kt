package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidFileUploadWorkSchedulerTest {
    private lateinit var context: Context
    private lateinit var fileUploadWork: FileUploadWork

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        fileUploadWork = mockk<FileUploadWork>()
        coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } returns Unit

        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration
                .Builder()
                .setExecutor(SynchronousExecutor())
                .setWorkerFactory(mockFileUploadWorkerFactory(fileUploadWork = fileUploadWork))
                .build(),
        )
    }

    @Test
    fun `TC-FILE-STORAGE-DATA-021 연결 조건 없이 요청하자 곧바로 올리기를 시작한다`() {
        runBlocking {
            val scheduler = AndroidFileUploadWorkScheduler(context = context)
            val request = request()

            scheduler.upload(request = request)
            awaitFinished()

            workInfoList().single().constraints.requiredNetworkType shouldBe NetworkType.NOT_REQUIRED
            coVerify(exactly = 1) { fileUploadWork.doWork(request = request, onStep = any()) }
            scheduler.state.first() shouldBe FileUploadState.Idle
            scheduler.isUploading() shouldBe false
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-009 올리는 중인 파일이 끝나기 전에 다른 파일을 요청하면 앞선 파일만 올린다`() {
        runBlocking {
            val pending = CompletableDeferred<Unit>()
            coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } coAnswers { pending.await() }
            val scheduler = AndroidFileUploadWorkScheduler(context = context)
            val first = request()
            val second = request()

            scheduler.upload(request = first)
            scheduler.state.first { state -> state is FileUploadState.Uploading }
            scheduler.upload(request = second)
            pending.complete(Unit)
            awaitFinished()

            coVerify(exactly = 1) { fileUploadWork.doWork(request = first, onStep = any()) }
            coVerify(exactly = 0) { fileUploadWork.doWork(request = second, onStep = any()) }
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DATA-021 연결 문제로 올리지 못하면 다시 시도하지 않고 실패로 끝나 올리는 파일이 없는 상태가 된다`() {
        runBlocking {
            coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } throws IOException(fixtureMonkey.giveMeOne<String>())
            val scheduler = AndroidFileUploadWorkScheduler(context = context)

            scheduler.upload(request = request())
            awaitFinished()

            workInfoList().single().state shouldBe WorkInfo.State.FAILED
            coVerify(exactly = 1) { fileUploadWork.doWork(request = any(), onStep = any()) }
            scheduler.state.first() shouldBe FileUploadState.Idle
        }
    }

    @Test
    fun `앞선 올리기가 끝난 뒤에는 새 파일을 올린다`() {
        runBlocking {
            val scheduler = AndroidFileUploadWorkScheduler(context = context)
            val first = request()
            val second = request()

            scheduler.upload(request = first)
            awaitFinished()
            scheduler.upload(request = second)
            awaitFinished()

            coVerify(exactly = 1) { fileUploadWork.doWork(request = second, onStep = any()) }
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-014 로그아웃하면 올리는 중인 올리기를 취소하고 붙들고 있던 파일을 돌려준다`() {
        runBlocking {
            coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } coAnswers { awaitCancellation() }
            val scheduler = AndroidFileUploadWorkScheduler(context = context)
            val request = request()

            scheduler.upload(request = request)
            scheduler.state.first { state -> state is FileUploadState.Uploading }
            val cancelledUriList = scheduler.cancel()
            awaitFinished()

            cancelledUriList shouldBe listOf(request.uri)
            workInfoList().single().state shouldBe WorkInfo.State.CANCELLED
            scheduler.state.first() shouldBe FileUploadState.Idle
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-015 다시 올릴 예정인 올리기는 로그아웃하면 실행되지 않고 취소된다`() {
        runBlocking {
            val scheduler = AndroidFileUploadWorkScheduler(context = context)
            val request = request()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    AndroidFileUploadWorkScheduler.FILE_UPLOAD_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<FileUploadWorker>()
                        .setInputData(request.toData())
                        .addTag(request.uri.toFileUploadTag())
                        .setInitialDelay(1, TimeUnit.HOURS)
                        .build(),
                ).result
                .get()

            val cancelledUriList = scheduler.cancel()
            awaitFinished()

            cancelledUriList shouldBe listOf(request.uri)
            workInfoList().single().state shouldBe WorkInfo.State.CANCELLED
            coVerify(exactly = 0) { fileUploadWork.doWork(request = any(), onStep = any()) }
        }
    }

    private fun request(): FileUploadRequest = FileUploadRequest(uri = fixtureMonkey.fileUri(), accountId = fixtureMonkey.giveMeOne<Uuid>())

    private suspend fun awaitFinished() {
        withTimeout(AWAIT_TIMEOUT) {
            WorkManager
                .getInstance(context)
                .getWorkInfosForUniqueWorkFlow(AndroidFileUploadWorkScheduler.FILE_UPLOAD_WORK_NAME)
                .first { workInfoList -> workInfoList.isNotEmpty() && workInfoList.all { workInfo -> workInfo.state.isFinished } }
        }
    }

    private fun workInfoList(): List<WorkInfo> =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(AndroidFileUploadWorkScheduler.FILE_UPLOAD_WORK_NAME)
            .get()
}

private val AWAIT_TIMEOUT = 5.seconds
