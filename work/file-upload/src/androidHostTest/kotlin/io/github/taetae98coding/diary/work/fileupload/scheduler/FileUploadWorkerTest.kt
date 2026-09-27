package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.app.Notification
import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.ForegroundUpdater
import androidx.work.ListenableWorker
import androidx.work.impl.utils.futures.SettableFuture
import androidx.work.testing.TestListenableWorkerBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadNotifier
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResult
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResultReporter
import io.github.taetae98coding.diary.work.fileupload.state.FileHomeViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWorkImpl
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FileUploadWorkerTest {
    private lateinit var context: Context
    private lateinit var fileUploadWork: FileUploadWork

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        fileUploadWork = mockk<FileUploadWork>()
    }

    @Test
    fun `맡긴 파일과 계정으로 올리기를 실행하고 성공으로 끝낸다`() {
        val request = FileUploadRequest(uri = fixtureMonkey.fileUri(), accountId = fixtureMonkey.giveMeOne<Uuid>())
        coEvery { fileUploadWork.doWork(request = request, onStep = any()) } returns Unit

        val result = runBlocking { worker(request = request).doWork() }

        result shouldBe ListenableWorker.Result.success()
        coVerify(exactly = 1) { fileUploadWork.doWork(request = request, onStep = any()) }
    }

    @Test
    fun `TC-FILE-STORAGE-DATA-021 연결 문제로 올리지 못하면 다시 시도하지 않고 실패로 끝낸다`() {
        val request = FileUploadRequest(uri = fixtureMonkey.fileUri(), accountId = fixtureMonkey.giveMeOne<Uuid>())
        coEvery { fileUploadWork.doWork(request = request, onStep = any()) } throws IOException(fixtureMonkey.giveMeOne<String>())

        val result = runBlocking { worker(request = request).doWork() }

        result shouldBe ListenableWorker.Result.failure()
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-002 FileHome을 보고 있어도 올리기를 시작하면 진행 알림을 띄운다`() {
        val request = FileUploadRequest(uri = fixtureMonkey.fileUri(), accountId = fixtureMonkey.giveMeOne<Uuid>())
        val source = fixtureMonkey.fileUploadSource().copy(uri = request.uri)
        val file = fixtureMonkey.giveMeOne<DiaryFile>()
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        every { uploadFileUseCase(parameter = any()) } returns
            flowOf(
                Result.success(FileUploadStep.Started(source = source)),
                Result.success(FileUploadStep.Completed(source = source, file = file)),
            )
        val viewingHolder = FileHomeViewingHolder().apply { isViewing = true }
        val work =
            FileUploadWorkImpl(
                uploadFileUseCase = uploadFileUseCase,
                fileRepository = mockk<FileRepository>(relaxed = true),
                fileUploadResultReporter =
                    FileUploadResultReporter(
                        fileHomeViewingHolder = viewingHolder,
                        fileUploadEventHolder = FileUploadEventHolder(),
                        fileUploadNotifier = mockk<FileUploadNotifier>(relaxed = true),
                    ),
            )
        val foregroundInfoSlot = slot<ForegroundInfo>()
        val foregroundUpdater =
            mockk<ForegroundUpdater> {
                every { setForegroundAsync(any(), any(), capture(foregroundInfoSlot)) } returns SettableFuture.create<Void?>().apply { set(null) }
            }

        val result = runBlocking { worker(request = request, work = work, foregroundUpdater = foregroundUpdater).doWork() }

        result shouldBe ListenableWorker.Result.success()
        foregroundInfoSlot.captured.notification.extras
            .getString(Notification.EXTRA_TEXT) shouldBe source.name
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-018 진행 알림을 띄우지 못해도 올리기는 끝까지 진행되고 결과를 알린다`() {
        val request = FileUploadRequest(uri = fixtureMonkey.fileUri(), accountId = fixtureMonkey.giveMeOne<Uuid>())
        val source = fixtureMonkey.fileUploadSource().copy(uri = request.uri)
        val file = fixtureMonkey.giveMeOne<DiaryFile>()
        val uploadFileUseCase = mockk<UploadFileUseCase>()
        every { uploadFileUseCase(parameter = any()) } returns
            flowOf(
                Result.success(FileUploadStep.Started(source = source)),
                Result.success(FileUploadStep.Completed(source = source, file = file)),
            )
        val notifier = mockk<FileUploadNotifier>(relaxed = true)
        val work =
            FileUploadWorkImpl(
                uploadFileUseCase = uploadFileUseCase,
                fileRepository = mockk<FileRepository>(relaxed = true),
                fileUploadResultReporter =
                    FileUploadResultReporter(
                        fileHomeViewingHolder = FileHomeViewingHolder(),
                        fileUploadEventHolder = FileUploadEventHolder(),
                        fileUploadNotifier = notifier,
                    ),
            )
        val foregroundUpdater =
            mockk<ForegroundUpdater> {
                every { setForegroundAsync(any(), any(), any()) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
            }

        val result = runBlocking { worker(request = request, work = work, foregroundUpdater = foregroundUpdater).doWork() }

        result shouldBe ListenableWorker.Result.success()
        verify(exactly = 1) { notifier.notifyResult(result = FileUploadResult.Succeeded(name = source.name, fileId = file.id)) }
    }

    private fun worker(request: FileUploadRequest): FileUploadWorker =
        TestListenableWorkerBuilder<FileUploadWorker>(context)
            .setInputData(request.toData())
            .setWorkerFactory(mockFileUploadWorkerFactory(fileUploadWork = fileUploadWork))
            .build()

    private fun worker(
        request: FileUploadRequest,
        work: FileUploadWork,
        foregroundUpdater: ForegroundUpdater,
    ): FileUploadWorker =
        TestListenableWorkerBuilder<FileUploadWorker>(context)
            .setInputData(request.toData())
            .setWorkerFactory(mockFileUploadWorkerFactory(fileUploadWork = work))
            .setForegroundUpdater(foregroundUpdater)
            .build()
}
