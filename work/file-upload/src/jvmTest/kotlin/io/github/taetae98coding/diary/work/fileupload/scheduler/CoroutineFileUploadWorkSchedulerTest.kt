@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.fileupload.scheduler

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUploadResult
import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.core.testing.file.fileUploadContent
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileRequest
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadNotifier
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResult
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResultReporter
import io.github.taetae98coding.diary.work.fileupload.state.FileScreenViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWorkImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class CoroutineFileUploadWorkSchedulerTest :
    BehaviorSpec({
        Given("파일 A를 올리는 중이고 서버가 아직 응답하지 않았다") {
            When("파일 B로 올리기를 요청한다") {
                Then("TC-FILE-STORAGE-DOMAIN-009 파일 B는 올리지 않고 파일 A의 올리기가 그대로 이어진다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)
                        val requestA = fixture.request()
                        val requestB = fixture.request()
                        coEvery { fixture.work.doWork(request = requestA, onStep = any()) } coAnswers { awaitCancellation() }
                        val scheduler = fixture.scheduler()

                        scheduler.upload(request = requestA)
                        runCurrent()
                        scheduler.upload(request = requestB)
                        runCurrent()

                        scheduler.isUploading() shouldBe true
                        coVerify(exactly = 1) { fixture.work.doWork(request = requestA, onStep = any()) }
                        coVerify(exactly = 0) { fixture.work.doWork(request = requestB, onStep = any()) }
                    }
                }
            }
        }

        Given("크기가 1,000바이트인 파일을 올린다") {
            When("보낸 양이 0, 250, 1,000바이트가 된 뒤 올리기가 끝난다") {
                Then("TC-FILE-STORAGE-DOMAIN-011 백분율 없음, 25%, 100%를 거쳐 올리는 파일이 없는 상태가 된다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)
                        val request = fixture.request()
                        val source = fixtureMonkey.fileUploadSource(size = 1_000).copy(uri = request.content.uri)
                        val next = MutableSharedFlow<Unit>()
                        coEvery { fixture.work.doWork(request = request, onStep = any()) } coAnswers {
                            val onStep = secondArg<suspend (FileUploadStep) -> Unit>()

                            listOf<FileUploadStep>(
                                FileUploadStep.Sent(source = source, sentBytes = 0),
                                FileUploadStep.Sent(source = source, sentBytes = 250),
                                FileUploadStep.Sent(source = source, sentBytes = 1_000),
                            ).forEach { step ->
                                next.first()
                                onStep(step)
                            }
                            next.first()
                        }
                        val scheduler = fixture.scheduler()
                        val percentList = mutableListOf<FileUploadState>()

                        scheduler.upload(request = request)
                        runCurrent()
                        repeat(3) {
                            next.emit(Unit)
                            runCurrent()
                            percentList += scheduler.state.first()
                        }
                        next.emit(Unit)
                        runCurrent()

                        percentList shouldBe
                            listOf(
                                FileUploadState.Uploading(percent = null),
                                FileUploadState.Uploading(percent = 25),
                                FileUploadState.Uploading(percent = 100),
                            )
                        scheduler.state.first() shouldBe FileUploadState.Idle
                        scheduler.isUploading() shouldBe false
                    }
                }
            }
        }

        Given("올리기가 실패로 끝난다") {
            When("올리기를 요청한다") {
                Then("올리는 파일이 없는 상태로 돌아와 다음 파일을 올릴 수 있다") {
                    runTest {
                        // 운영의 올리기 스코프처럼 실패한 작업의 예외가 다른 작업과 테스트를 끝내지 않게 받아 둔다.
                        val scope = CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]) + CoroutineExceptionHandler { _, _ -> })
                        val fixture = SchedulerFixture(scope = scope)
                        val first = fixture.request()
                        val second = fixture.request()
                        coEvery { fixture.work.doWork(request = first, onStep = any()) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
                        coEvery { fixture.work.doWork(request = second, onStep = any()) } returns Unit
                        val scheduler = fixture.scheduler()

                        scheduler.upload(request = first)
                        runCurrent()
                        scheduler.state.first() shouldBe FileUploadState.Idle

                        scheduler.upload(request = second)
                        runCurrent()
                        coVerify(exactly = 1) { fixture.work.doWork(request = second, onStep = any()) }
                    }
                }
            }
        }

        Given("데스크톱 앱이나 웹에서 연결이 없어 서버 요청이 연결 실패로 끝난다") {
            When("파일 하나로 올리기를 시작한다") {
                Then("TC-FILE-STORAGE-DATA-020 기다리지 않고 크기 초과가 아닌 실패로 알리고 올리는 파일이 없는 상태로 돌아간다") {
                    runTest {
                        val scope = CoroutineScope(backgroundScope.coroutineContext + SupervisorJob(backgroundScope.coroutineContext[Job]) + CoroutineExceptionHandler { _, _ -> })
                        val fixture = SchedulerFixture(scope = scope)
                        val request = fixture.request()
                        val source = fixtureMonkey.fileUploadSource().copy(uri = request.content.uri)
                        val uploadFileUseCase = mockk<UploadFileUseCase>()
                        every { uploadFileUseCase(parameter = UploadFileRequest(content = request.content, accountId = request.accountId)) } returns
                            flowOf(Result.success(FileUploadStep.Started(source = source)), Result.failure(IOException(fixtureMonkey.giveMeOne<String>())))
                        coEvery { fixture.fileRepository.removeUploadSource(uri = request.content.uri) } returns Unit
                        val scheduler =
                            fixture.scheduler(
                                work =
                                    FileUploadWorkImpl(
                                        uploadFileUseCase = uploadFileUseCase,
                                        fileRepository = fixture.fileRepository,
                                        fileUploadResultReporter = fixture.reporter,
                                    ),
                            )

                        scheduler.upload(request = request)
                        runCurrent()

                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Failed(name = source.name)) }
                        scheduler.state.first() shouldBe FileUploadState.Idle
                        scheduler.isUploading() shouldBe false
                    }
                }
            }
        }

        Given("사용자가 FileHome 화면에서 파일 올리기를 시작했고 서버가 아직 응답하지 않았다") {
            When("사용자가 FileHome 화면을 떠난 뒤 서버가 성공을 돌려준다") {
                Then("TC-FILE-STORAGE-DATA-024 올리기가 중단되지 않고 성공으로 끝나 결과를 알림으로 알린다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)
                        val request = fixture.request()
                        val source = fixtureMonkey.fileUploadSource().copy(uri = request.content.uri)
                        val file = fixtureMonkey.diaryFile()
                        val response = CompletableDeferred<Unit>()
                        val uploadFileUseCase = mockk<UploadFileUseCase>()
                        every { uploadFileUseCase(parameter = UploadFileRequest(content = request.content, accountId = request.accountId)) } returns
                            flow {
                                emit(Result.success(FileUploadStep.Started(source = source)))
                                response.await()
                                emit(Result.success(FileUploadStep.Completed(source = source, file = file)))
                            }
                        coEvery { fixture.fileRepository.removeUploadSource(uri = request.content.uri) } returns Unit
                        val viewingHolder = FileScreenViewingHolder().apply { start(screen = FileScreen.ADD) }
                        val notifier = mockk<FileUploadNotifier>(relaxed = true)
                        val scheduler =
                            fixture.scheduler(
                                work =
                                    FileUploadWorkImpl(
                                        uploadFileUseCase = uploadFileUseCase,
                                        fileRepository = fixture.fileRepository,
                                        fileUploadResultReporter =
                                            FileUploadResultReporter(
                                                fileScreenViewingHolder = viewingHolder,
                                                fileUploadEventHolder = FileUploadEventHolder(),
                                                fileUploadNotifier = notifier,
                                            ),
                                    ),
                            )

                        scheduler.upload(request = request)
                        runCurrent()
                        viewingHolder.stop(screen = FileScreen.ADD)
                        response.complete(Unit)
                        runCurrent()

                        verify(exactly = 1) { notifier.notifyResult(result = FileUploadResult.Succeeded(name = source.name, fileId = file.id)) }
                        scheduler.state.first() shouldBe FileUploadState.Idle
                    }
                }
            }
        }

        Given("앞선 실행이 올리려고 기기에 둔 사본이 남아 있을 수 있다") {
            When("올리기 예약기가 만들어진다") {
                Then("남은 사본을 지운다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)

                        fixture.scheduler()
                        runCurrent()

                        coVerify(exactly = 1) { fixture.fileRepository.deleteLeftoverUploadSources() }
                    }
                }
            }
        }

        Given("앞선 실행에서 시작한 올리기를 시스템이 이어서 보내고 있다") {
            When("상태를 확인하고 새 올리기를 요청한다") {
                Then("TC-FILE-STORAGE-DATA-018 올리는 중으로 보고 새 파일은 올리지 않는다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)
                        fixture.continuedUpload.value = ContinuedFileUpload(name = fixtureMonkey.fileUploadSource().name, size = 1_000, sentBytes = 250)
                        val scheduler = fixture.scheduler()
                        runCurrent()

                        scheduler.state.first() shouldBe FileUploadState.Uploading(percent = 25)
                        scheduler.isUploading() shouldBe true

                        scheduler.upload(request = fixture.request())
                        runCurrent()
                        coVerify(exactly = 0) { fixture.work.doWork(request = any(), onStep = any()) }
                    }
                }
            }

            When("이어서 보내던 올리기가 끝난다") {
                Then("TC-FILE-STORAGE-DATA-018 성공, 크기 초과, 그 밖의 실패를 올리기 결과로 알린다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)
                        val file = fixtureMonkey.diaryFile()
                        fixture.scheduler()
                        runCurrent()

                        fixture.continuedUploadResult.emit(ContinuedFileUploadResult.Succeeded(name = file.name, file = file))
                        fixture.continuedUploadResult.emit(ContinuedFileUploadResult.TooLarge(name = file.name))
                        fixture.continuedUploadResult.emit(ContinuedFileUploadResult.Failed(name = file.name))
                        runCurrent()

                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Succeeded(name = file.name, fileId = file.id)) }
                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.TooLarge) }
                        verify(exactly = 1) { fixture.reporter.report(result = FileUploadResult.Failed(name = file.name)) }
                    }
                }
            }
        }

        Given("계정 A가 시작한 파일을 올리는 중이다") {
            listOf<Pair<String, Uuid?>>(
                "게스트" to null,
                "계정 B의 사용자" to fixtureMonkey.giveMeOne<Uuid>(),
            ).forEach { (accountState, exceptAccountId) ->
                When("${accountState}가 확인되어 다른 계정의 올리기를 취소한다") {
                    Then("TC-FILE-STORAGE-DOMAIN-017 올리기를 멈추고 결과를 알리지 않으며 붙들고 있던 파일을 돌려준다") {
                        runTest {
                            val fixture = SchedulerFixture(scope = backgroundScope)
                            val request = fixture.request()
                            val isCancelled = CompletableDeferred<Unit>()
                            coEvery { fixture.work.doWork(request = request, onStep = any()) } coAnswers {
                                try {
                                    awaitCancellation()
                                } finally {
                                    isCancelled.complete(Unit)
                                }
                            }
                            val scheduler = fixture.scheduler()
                            scheduler.upload(request = request)
                            runCurrent()

                            val cancelledUriList = scheduler.cancel(exceptAccountId = exceptAccountId)
                            runCurrent()

                            cancelledUriList shouldBe listOf(request.content.uri)
                            isCancelled.isCompleted shouldBe true
                            scheduler.state.first() shouldBe FileUploadState.Idle
                            verify(exactly = 0) { fixture.reporter.report(result = any()) }
                        }
                    }
                }
            }

            When("계정 A가 확인되어 다른 계정의 올리기를 취소한다") {
                Then("TC-FILE-STORAGE-DOMAIN-017 올리기가 계속되고 돌려줄 파일이 없다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)
                        val request = fixture.request()
                        coEvery { fixture.work.doWork(request = request, onStep = any()) } coAnswers { awaitCancellation() }
                        val scheduler = fixture.scheduler()
                        scheduler.upload(request = request)
                        runCurrent()

                        val cancelledUriList = scheduler.cancel(exceptAccountId = fixture.account.id)
                        runCurrent()

                        cancelledUriList shouldBe emptyList()
                        scheduler.isUploading() shouldBe true
                        scheduler.state.first() shouldBe FileUploadState.Uploading(percent = null)
                    }
                }
            }
        }

        Given("올리는 파일이 없다") {
            When("게스트가 확인되어 올리기를 취소한다") {
                Then("돌려줄 파일이 없다") {
                    runTest {
                        val fixture = SchedulerFixture(scope = backgroundScope)

                        fixture.scheduler().cancel(exceptAccountId = null) shouldBe emptyList()
                    }
                }
            }
        }
    })

private class SchedulerFixture(
    private val scope: CoroutineScope,
) {
    val account = fixtureMonkey.giveMeOne<Account.User>()
    val work = mockk<FileUploadWork>()
    val continuedUpload = MutableStateFlow<ContinuedFileUpload?>(null)
    val continuedUploadResult = MutableSharedFlow<ContinuedFileUploadResult>()
    val fileRepository =
        mockk<FileRepository> {
            every { getContinuedUpload() } returns continuedUpload
            every { getContinuedUploadResult() } returns continuedUploadResult
            coEvery { deleteContinuedUpload(exceptAccountId = any()) } returns Unit
            coEvery { deleteLeftoverUploadSources() } returns Unit
        }
    val reporter =
        mockk<FileUploadResultReporter> {
            every { report(result = any()) } returns Unit
        }

    private var requestCount = 0

    // 임의로 만든 위치가 우연히 겹쳐도 요청마다 다른 파일을 가리키게 한다.
    fun request(): FileUploadRequest = FileUploadRequest(content = fixtureMonkey.fileUploadContent(uri = FileUri("${fixtureMonkey.fileUri().value}-${requestCount++}")), accountId = account.id)

    fun scheduler(work: FileUploadWork = this.work): CoroutineFileUploadWorkScheduler =
        CoroutineFileUploadWorkScheduler(
            fileUploadWork = work,
            fileRepository = fileRepository,
            fileUploadResultReporter = reporter,
            scope = scope,
        )
}
