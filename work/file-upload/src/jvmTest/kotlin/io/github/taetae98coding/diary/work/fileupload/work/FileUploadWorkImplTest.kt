package io.github.taetae98coding.diary.work.fileupload.work

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUnreadableException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileRequest
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResult
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResultReporter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileUploadWorkImplTest :
    BehaviorSpec({
        Given("고른 파일을 서버에 보관할 수 있다") {
            When("올리기를 실행한다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-004 단계를 차례로 전달하고 올린 파일의 이름과 식별자로 성공을 알린다") {
                    runTest {
                        val fixture = WorkFixture()
                        val source = fixture.source()
                        val file = fixtureMonkey.diaryFile()
                        val stepList =
                            listOf(
                                FileUploadStep.Started(source = source),
                                FileUploadStep.Sent(source = source, sentBytes = source.size),
                                FileUploadStep.Completed(source = source, file = file),
                            )
                        fixture.emit(flowOf(*stepList.map { step -> Result.success(step) }.toTypedArray()))
                        val receivedStepList = mutableListOf<FileUploadStep>()

                        fixture.work.doWork(request = fixture.request) { step -> receivedStepList += step }

                        receivedStepList shouldBe stepList
                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Succeeded(name = source.name, fileId = file.id)) }
                        coVerify(exactly = 1) { fixture.fileRepository.removeUploadSource(uri = fixture.request.uri) }
                    }
                }
            }
        }

        Given("고른 파일이 올릴 수 있는 크기를 넘는다") {
            When("올리기를 실행한다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-004 크기 초과를 알리고 실패를 그대로 던진다") {
                    runTest {
                        val fixture = WorkFixture()
                        val exception = FileTooLargeException(message = fixtureMonkey.giveMeOne<String>())
                        fixture.emit(flowOf(Result.failure(exception)))

                        val thrown = shouldThrow<FileTooLargeException> { fixture.work.doWork(request = fixture.request) {} }

                        thrown shouldBeSameInstanceAs exception
                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.TooLarge) }
                        coVerify(exactly = 1) { fixture.fileRepository.removeUploadSource(uri = fixture.request.uri) }
                    }
                }
            }
        }

        Given("올리기를 시작한 뒤 그 밖의 이유로 올리지 못했다") {
            When("올리기를 실행한다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-004 고른 파일의 이름으로 실패를 알린다") {
                    runTest {
                        val fixture = WorkFixture()
                        val source = fixture.source()
                        fixture.emit(flowOf(Result.success(FileUploadStep.Started(source = source)), Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))))

                        shouldThrow<IllegalStateException> { fixture.work.doWork(request = fixture.request) {} }

                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Failed(name = source.name)) }
                    }
                }
            }
        }

        Given("파일 이름은 읽었지만 크기를 읽지 못했다") {
            When("올리기를 실행한다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-004 읽어 둔 파일 이름으로 실패를 알린다") {
                    runTest {
                        val fixture = WorkFixture()
                        val name = fixture.source().name
                        fixture.emit(flowOf(Result.failure(FileUnreadableException(name = name, cause = IllegalStateException(fixtureMonkey.giveMeOne<String>())))))

                        shouldThrow<FileUnreadableException> { fixture.work.doWork(request = fixture.request) {} }

                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Failed(name = name)) }
                    }
                }
            }
        }

        Given("올리기를 시작한 뒤 연결 문제로 보내지 못했다") {
            When("올리기를 실행한다") {
                Then("TC-FILE-STORAGE-DATA-021 다시 시도하지 않고 크기 초과가 아닌 실패로 알린 뒤 실패를 던진다") {
                    runTest {
                        val fixture = WorkFixture()
                        val source = fixture.source()
                        fixture.emit(flowOf(Result.success(FileUploadStep.Started(source = source)), Result.failure(IOException(fixtureMonkey.giveMeOne<String>()))))

                        shouldThrow<IOException> { fixture.work.doWork(request = fixture.request) {} }

                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Failed(name = source.name)) }
                        verify(exactly = 1) { fixture.uploadFileUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("붙들어 둔 파일을 올리는 중이다") {
            When("올리기가 성공이나 실패로 끝난다") {
                Then("TC-FILE-STORAGE-DATA-023 붙들어 둔 파일을 놓는다") {
                    listOf(
                        { source: FileUploadSource -> Result.success(FileUploadStep.Completed(source = source, file = fixtureMonkey.giveMeOne<DiaryFile>())) },
                        { _: FileUploadSource -> Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())) },
                    ).forEach { last ->
                        runTest {
                            val fixture = WorkFixture()
                            val source = fixture.source()
                            fixture.emit(flowOf(Result.success(FileUploadStep.Started(source = source)), last(source)))

                            runCatching { fixture.work.doWork(request = fixture.request) {} }

                            coVerify(exactly = 1) { fixture.fileRepository.removeUploadSource(uri = fixture.request.uri) }
                        }
                    }
                }
            }
        }

        Given("파일 이름을 읽기 전에 올리지 못했다") {
            When("올리기를 실행한다") {
                Then("TC-FILE-UPLOAD-NOTIFICATION-FEATURE-015 이름을 알 수 없는 실패로 알리고 붙들어 둔 파일을 놓는다") {
                    runTest {
                        val fixture = WorkFixture()
                        fixture.emit(flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))))

                        shouldThrow<IllegalStateException> { fixture.work.doWork(request = fixture.request) {} }

                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Failed(name = "")) }
                        coVerify(exactly = 1) { fixture.fileRepository.removeUploadSource(uri = fixture.request.uri) }
                    }
                }
            }
        }

        Given("올리는 동안 계정이 바뀌었다") {
            When("올리기를 실행한다") {
                Then("TC-FILE-STORAGE-DOMAIN-010 TC-FILE-UPLOAD-NOTIFICATION-FEATURE-006 TC-FILE-STORAGE-DATA-023 TC-FILE-HOME-FEATURE-024 결과를 알리지 않고 중단으로 끝내며 붙들어 둔 파일을 놓는다") {
                    runTest {
                        val fixture = WorkFixture()
                        fixture.emit(flowOf(Result.failure(FileUploadAccountChangedException(message = fixtureMonkey.giveMeOne<String>()))))

                        shouldThrow<FileUploadAccountChangedException> { fixture.work.doWork(request = fixture.request) {} }

                        verify(exactly = 0) { fixture.reporter.report(result = any()) }
                        coVerify(exactly = 1) { fixture.fileRepository.removeUploadSource(uri = fixture.request.uri) }
                    }
                }
            }
        }

        Given("올리는 중이다") {
            When("올리기가 취소된다") {
                Then("TC-FILE-STORAGE-DATA-023 결과를 알리지 않고, 다시 올릴 수 있게 붙들어 둔 파일을 놓지 않는다") {
                    runTest {
                        val fixture = WorkFixture()
                        fixture.emit(flow { awaitCancellation() })

                        val job = launch { fixture.work.doWork(request = fixture.request) {} }
                        runCurrent()
                        job.cancel()
                        runCurrent()

                        verify(exactly = 0) { fixture.reporter.report(result = any()) }
                        coVerify(exactly = 0) { fixture.fileRepository.removeUploadSource(uri = any()) }
                    }
                }
            }
        }
    })

private class WorkFixture {
    val request = FileUploadRequest(uri = fixtureMonkey.fileUri(), accountId = fixtureMonkey.giveMeOne<Uuid>())
    val uploadFileUseCase = mockk<UploadFileUseCase>()
    val fileRepository =
        mockk<FileRepository> {
            coEvery { removeUploadSource(uri = any()) } returns Unit
        }
    val reporter =
        mockk<FileUploadResultReporter> {
            every { report(result = any()) } returns Unit
        }
    val work =
        FileUploadWorkImpl(
            uploadFileUseCase = uploadFileUseCase,
            fileRepository = fileRepository,
            fileUploadResultReporter = reporter,
        )

    fun source(): FileUploadSource = fixtureMonkey.fileUploadSource().copy(uri = request.uri)

    fun emit(flow: Flow<Result<FileUploadStep>>) {
        every { uploadFileUseCase(parameter = UploadFileRequest(uri = request.uri, accountId = request.accountId)) } returns flow
    }
}
