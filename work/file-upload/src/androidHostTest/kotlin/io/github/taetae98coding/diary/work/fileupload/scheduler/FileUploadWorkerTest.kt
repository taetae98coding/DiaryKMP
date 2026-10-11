package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.app.Notification
import android.content.Context
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.ForegroundUpdater
import androidx.work.ListenableWorker
import androidx.work.impl.utils.futures.SettableFuture
import androidx.work.testing.TestListenableWorkerBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.testing.file.fileUploadContent
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadNotifier
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResult
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResultReporter
import io.github.taetae98coding.diary.work.fileupload.state.FileScreenViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import io.github.taetae98coding.diary.work.fileupload.text.FileUploadText
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWorkImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
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
    fun `맡긴 파일과 계정으로 올리기를 실행하고 성공으로 끝낸다`() =
        runTest {
            val request = FileUploadRequest(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())
            coEvery { fileUploadWork.doWork(request = request, onStep = any()) } returns Unit

            val result = worker(request = request, dispatcher = StandardTestDispatcher(testScheduler)).doWork()

            result shouldBe ListenableWorker.Result.success()
            coVerify(exactly = 1) { fileUploadWork.doWork(request = request, onStep = any()) }
        }

    @Test
    fun `TC-FILE-STORAGE-DATA-021 연결 문제로 올리지 못하면 다시 시도하지 않고 실패로 끝내고 기기에 둔 제목과 설명을 지운다`() =
        runTest {
            val request = FileUploadRequest(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())
            coEvery { fileUploadWork.doWork(request = request, onStep = any()) } throws IOException(fixtureMonkey.giveMeOne<String>())

            val result = worker(request = request, dispatcher = StandardTestDispatcher(testScheduler)).doWork()

            result shouldBe ListenableWorker.Result.failure()
            textFileList().shouldBeEmpty()
        }

    @Test
    fun `성공으로 끝나면 기기에 둔 제목과 설명을 지운다`() =
        runTest {
            val request = FileUploadRequest(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())
            coEvery { fileUploadWork.doWork(request = request, onStep = any()) } returns Unit

            worker(request = request, dispatcher = StandardTestDispatcher(testScheduler)).doWork()

            textFileList().shouldBeEmpty()
        }

    @Test
    fun `TC-FILE-STORAGE-DATA-028 시스템이 작업을 멈춘 뒤 다시 실행하면 같은 제목과 설명으로 다시 올린다`() =
        runTest {
            val request = FileUploadRequest(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())
            val inputData = inputData(request = request, dispatcher = StandardTestDispatcher(testScheduler))
            var runCount = 0
            coEvery { fileUploadWork.doWork(request = request, onStep = any()) } answers {
                runCount++
                if (runCount == 1) throw CancellationException(fixtureMonkey.giveMeOne<String>())
            }

            shouldThrow<CancellationException> { worker(inputData = inputData, dispatcher = StandardTestDispatcher(testScheduler)).doWork() }
            textFileList().size shouldBe 1
            val result = worker(inputData = inputData, dispatcher = StandardTestDispatcher(testScheduler)).doWork()

            result shouldBe ListenableWorker.Result.success()
            coVerify(exactly = 2) { fileUploadWork.doWork(request = request, onStep = any()) }
            textFileList().shouldBeEmpty()
        }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-002 FileHome을 보고 있어도 올리기를 시작하면 진행 알림을 띄운다`() =
        runTest {
            val request = FileUploadRequest(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())
            val source = fixtureMonkey.fileUploadSource().copy(uri = request.content.uri)
            val file = fixtureMonkey.giveMeOne<DiaryFile>()
            val uploadFileUseCase = mockk<UploadFileUseCase>()
            every { uploadFileUseCase(parameter = any()) } returns
                flowOf(
                    Result.success(FileUploadStep.Started(source = source)),
                    Result.success(FileUploadStep.Completed(source = source, file = file)),
                )
            val viewingHolder = FileScreenViewingHolder().apply { start(screen = FileScreen.HOME) }
            val work =
                FileUploadWorkImpl(
                    uploadFileUseCase = uploadFileUseCase,
                    fileRepository = mockk<FileRepository>(relaxed = true),
                    fileUploadResultReporter =
                        FileUploadResultReporter(
                            fileScreenViewingHolder = viewingHolder,
                            fileUploadEventHolder = FileUploadEventHolder(),
                            fileUploadNotifier = mockk<FileUploadNotifier>(relaxed = true),
                        ),
                )
            val foregroundInfoSlot = slot<ForegroundInfo>()
            val foregroundUpdater =
                mockk<ForegroundUpdater> {
                    every { setForegroundAsync(any(), any(), capture(foregroundInfoSlot)) } returns SettableFuture.create<Void?>().apply { set(null) }
                }

            val result = worker(request = request, work = work, foregroundUpdater = foregroundUpdater, dispatcher = StandardTestDispatcher(testScheduler)).doWork()

            result shouldBe ListenableWorker.Result.success()
            foregroundInfoSlot.captured.notification.extras
                .getString(Notification.EXTRA_TEXT) shouldBe source.name
        }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-018 진행 알림을 띄우지 못해도 올리기는 끝까지 진행되고 결과를 알린다`() =
        runTest {
            val request = FileUploadRequest(content = fixtureMonkey.fileUploadContent(), accountId = fixtureMonkey.giveMeOne<Uuid>())
            val source = fixtureMonkey.fileUploadSource().copy(uri = request.content.uri)
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
                            fileScreenViewingHolder = FileScreenViewingHolder(),
                            fileUploadEventHolder = FileUploadEventHolder(),
                            fileUploadNotifier = notifier,
                        ),
                )
            val foregroundUpdater =
                mockk<ForegroundUpdater> {
                    every { setForegroundAsync(any(), any(), any()) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
                }

            val result = worker(request = request, work = work, foregroundUpdater = foregroundUpdater, dispatcher = StandardTestDispatcher(testScheduler)).doWork()

            result shouldBe ListenableWorker.Result.success()
            verify(exactly = 1) { notifier.notifyResult(result = FileUploadResult.Succeeded(name = source.name, fileId = file.id)) }
        }

    private suspend fun worker(
        request: FileUploadRequest,
        dispatcher: CoroutineDispatcher,
    ): FileUploadWorker = worker(inputData = inputData(request = request, dispatcher = dispatcher), dispatcher = dispatcher)

    private fun worker(
        inputData: Data,
        dispatcher: CoroutineDispatcher,
    ): FileUploadWorker =
        TestListenableWorkerBuilder<FileUploadWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(mockFileUploadWorkerFactory(fileUploadWork = fileUploadWork, dispatcher = dispatcher))
            .build()

    private suspend fun inputData(
        request: FileUploadRequest,
        dispatcher: CoroutineDispatcher,
    ): Data {
        val textId = fileUploadTextStore(context = context, dispatcher = dispatcher).write(text = FileUploadText(title = request.content.title, description = request.content.description))

        return request.toData(textId = textId)
    }

    private fun textFileList(): List<File> = File(context.noBackupFilesDir, "file-upload-text").listFiles().orEmpty().toList()

    private suspend fun worker(
        request: FileUploadRequest,
        work: FileUploadWork,
        foregroundUpdater: ForegroundUpdater,
        dispatcher: CoroutineDispatcher,
    ): FileUploadWorker =
        TestListenableWorkerBuilder<FileUploadWorker>(context)
            .setInputData(inputData(request = request, dispatcher = dispatcher))
            .setWorkerFactory(mockFileUploadWorkerFactory(fileUploadWork = work, dispatcher = dispatcher))
            .setForegroundUpdater(foregroundUpdater)
            .build()
}
