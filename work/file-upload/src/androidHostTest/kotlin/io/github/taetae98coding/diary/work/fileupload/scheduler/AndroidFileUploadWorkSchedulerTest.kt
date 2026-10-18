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
import io.github.taetae98coding.diary.core.testing.file.fileUploadContent
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.text.FileUploadText
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
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

    // WorkManager가 만드는 작업자도 테스트와 같은 스케줄러를 쓰도록 runTest보다 먼저 만들어 둔다.
    private val testDispatcher = StandardTestDispatcher()

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
                .setWorkerFactory(mockFileUploadWorkerFactory(fileUploadWork = fileUploadWork, dispatcher = testDispatcher))
                .build(),
        )
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-016 맡긴 파일의 제목과 설명을 그대로 담아 올리기를 실행하고 끝나면 기기에 둔 제목과 설명을 지운다`() {
        runTest(testDispatcher) {
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
            val request = request()

            scheduler.upload(request = request)
            awaitFinished()

            coVerify(exactly = 1) { fileUploadWork.doWork(request = request, onStep = any()) }
            textFileList().shouldBeEmpty()
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DATA-021 연결 조건 없이 요청하자 곧바로 올리기를 시작한다`() {
        runTest(testDispatcher) {
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
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
        runTest(testDispatcher) {
            val pending = CompletableDeferred<Unit>()
            coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } coAnswers { pending.await() }
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
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
        runTest(testDispatcher) {
            coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } throws IOException(fixtureMonkey.giveMeOne<String>())
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))

            scheduler.upload(request = request())
            awaitFinished()

            workInfoList().single().state shouldBe WorkInfo.State.FAILED
            coVerify(exactly = 1) { fileUploadWork.doWork(request = any(), onStep = any()) }
            scheduler.state.first() shouldBe FileUploadState.Idle
        }
    }

    @Test
    fun `앞선 올리기가 끝난 뒤에는 새 파일을 올린다`() {
        runTest(testDispatcher) {
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
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
    fun `TC-FILE-STORAGE-DOMAIN-017 게스트나 다른 계정이 확인되면 올리는 중인 올리기를 취소하고 붙들고 있던 파일을 돌려준다`() {
        runTest(testDispatcher) {
            listOf<Uuid?>(null, fixtureMonkey.giveMeOne<Uuid>()).forEach { exceptAccountId ->
                coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } coAnswers { awaitCancellation() }
                val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
                val request = request()

                scheduler.upload(request = request)
                scheduler.state.first { state -> state is FileUploadState.Uploading }
                val cancelledUriList = scheduler.cancel(exceptAccountId = exceptAccountId)
                awaitFinished()

                cancelledUriList shouldBe listOf(request.content.uri)
                textFileList().shouldBeEmpty()
                workInfoList().all { workInfo -> workInfo.state == WorkInfo.State.CANCELLED } shouldBe true
                scheduler.state.first() shouldBe FileUploadState.Idle
            }
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-017 올리기를 시작한 계정이 확인되면 올리는 중인 올리기를 그대로 둔다`() {
        runTest(testDispatcher) {
            coEvery { fileUploadWork.doWork(request = any(), onStep = any()) } coAnswers { awaitCancellation() }
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
            val request = request()

            scheduler.upload(request = request)
            scheduler.state.first { state -> state is FileUploadState.Uploading }
            val cancelledUriList = scheduler.cancel(exceptAccountId = request.accountId)

            cancelledUriList.shouldBeEmpty()
            textFileList().size shouldBe 1
            workInfoList().single().state shouldBe WorkInfo.State.RUNNING
            scheduler.isUploading() shouldBe true

            scheduler.cancel(exceptAccountId = null)
            awaitFinished()
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-019 다시 올릴 예정인 올리기는 게스트나 다른 계정이 확인되면 실행되지 않고 취소된다`() {
        runTest(testDispatcher) {
            listOf<Uuid?>(null, fixtureMonkey.giveMeOne<Uuid>()).forEach { exceptAccountId ->
                val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
                val request = request()
                enqueueDelayedWork(request = request, accountTag = request.accountId.toFileUploadAccountTag())

                val cancelledUriList = scheduler.cancel(exceptAccountId = exceptAccountId)
                awaitFinished()

                cancelledUriList shouldBe listOf(request.content.uri)
                textFileList().shouldBeEmpty()
                workInfoList().all { workInfo -> workInfo.state == WorkInfo.State.CANCELLED } shouldBe true
                coVerify(exactly = 0) { fileUploadWork.doWork(request = any(), onStep = any()) }
            }
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-019 다시 올릴 예정인 올리기는 시작한 계정이 확인되면 그대로 남는다`() {
        runTest(testDispatcher) {
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
            val request = request()
            enqueueDelayedWork(request = request, accountTag = request.accountId.toFileUploadAccountTag())

            val cancelledUriList = scheduler.cancel(exceptAccountId = request.accountId)

            cancelledUriList.shouldBeEmpty()
            textFileList().size shouldBe 1
            workInfoList().single().state shouldBe WorkInfo.State.ENQUEUED
        }
    }

    @Test
    fun `TC-FILE-STORAGE-DOMAIN-018 시작한 계정을 알 수 없는 앞선 버전의 올리기는 사용자 계정이 확인되어도 취소된다`() {
        runTest(testDispatcher) {
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
            val request = request()
            enqueueDelayedWork(request = request, accountTag = null)

            val cancelledUriList = scheduler.cancel(exceptAccountId = request.accountId)
            awaitFinished()

            cancelledUriList shouldBe listOf(request.content.uri)
            textFileList().shouldBeEmpty()
            workInfoList().single().state shouldBe WorkInfo.State.CANCELLED
        }
    }

    @Test
    fun `올리기를 넣으면 시작한 계정을 함께 남긴다`() {
        runTest(testDispatcher) {
            val scheduler = AndroidFileUploadWorkScheduler(context = context, fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = testDispatcher))
            val request = request()

            scheduler.upload(request = request)
            awaitFinished()

            workInfoList().single().tags.contains(request.accountId.toFileUploadAccountTag()) shouldBe true
        }
    }

    // 시스템이 앱을 정리해 다시 올리려고 기다리는 작업을 흉내 낸다.
    private suspend fun enqueueDelayedWork(
        request: FileUploadRequest,
        accountTag: String?,
    ) {
        val textId = fileUploadTextStore(context = context, dispatcher = testDispatcher).write(text = FileUploadText(title = request.content.title, description = request.content.description))

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                AndroidFileUploadWorkScheduler.FILE_UPLOAD_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<FileUploadWorker>()
                    .setInputData(request.toData(textId = textId))
                    .addTag(request.content.uri.toFileUploadTag())
                    .addTag(textId.toFileUploadTextTag())
                    .apply { accountTag?.let(::addTag) }
                    .setInitialDelay(1, TimeUnit.HOURS)
                    .build(),
            ).result
            .get()
    }

    private fun request(): FileUploadRequest = FileUploadRequest(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())

    private fun textFileList(): List<File> = File(context.noBackupFilesDir, "file-upload-text").listFiles().orEmpty().toList()

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
