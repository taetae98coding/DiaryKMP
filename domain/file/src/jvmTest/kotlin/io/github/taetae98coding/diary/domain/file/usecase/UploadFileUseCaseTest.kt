package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

private const val MAX_SIZE = 52_428_800L

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class UploadFileUseCaseTest :
    BehaviorSpec({
        Given("고른 파일을 서버에 보관할 수 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val source = fixtureMonkey.fileUploadSource()
            val partialSentBytes = source.size / 2
            val file = fixtureMonkey.giveMeOne<DiaryFile>()
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.readSource(uri = source.uri) } returns source
            coEvery { fileRepository.create(source = source, title = any(), description = any(), accountId = any(), onSent = any()) } answers {
                arg<(Long) -> Unit>(4).invoke(partialSentBytes)
                arg<(Long) -> Unit>(4).invoke(source.size)
                file
            }
            val useCase = useCase(accountFlow = MutableStateFlow(Result.success(account)), fileRepository = fileRepository)

            When("그 파일을 올린다") {
                val resultList = useCase(parameter = UploadFileRequest(content = content(uri = source.uri), accountId = account.id)).toList()

                Then("TC-FILE-STORAGE-DOMAIN-011 시작, 보낸 양, 보관한 파일을 차례로 알린다") {
                    resultList.map { result -> result.getOrThrow() } shouldBe
                        listOf(
                            FileUploadStep.Started(source = source),
                            FileUploadStep.Sent(source = source, sentBytes = partialSentBytes),
                            FileUploadStep.Sent(source = source, sentBytes = source.size),
                            FileUploadStep.Completed(source = source, file = file),
                        )
                }
            }
        }

        Given("고른 파일의 크기가 테스트 데이터와 같다") {
            When("그 파일을 올린다") {
                Then("TC-FILE-STORAGE-DOMAIN-001 50MB 이하는 서버에 올리고 넘으면 서버에 요청하지 않고 크기 초과로 실패한다") {
                    mapOf(
                        0L to true,
                        MAX_SIZE to true,
                        MAX_SIZE + 1 to false,
                    ).forEach { (size, isUploaded) ->
                        val account = fixtureMonkey.giveMeOne<Account.User>()
                        val source = fixtureMonkey.fileUploadSource(size = size)
                        val file = fixtureMonkey.giveMeOne<DiaryFile>()
                        val fileRepository = mockk<FileRepository>()
                        coEvery { fileRepository.readSource(uri = source.uri) } returns source
                        coEvery { fileRepository.create(source = source, title = any(), description = any(), accountId = any(), onSent = any()) } returns file
                        val useCase = useCase(accountFlow = MutableStateFlow(Result.success(account)), fileRepository = fileRepository)

                        val last = useCase(parameter = UploadFileRequest(content = content(uri = source.uri), accountId = account.id)).toList().last()

                        if (isUploaded) {
                            last.getOrThrow() shouldBe FileUploadStep.Completed(source = source, file = file)
                            coVerify(exactly = 1) { fileRepository.create(source = source, title = any(), description = any(), accountId = any(), onSent = any()) }
                        } else {
                            last.shouldBeFailure().shouldBeInstanceOf<FileTooLargeException>()
                            coVerify(exactly = 0) { fileRepository.create(source = any(), title = any(), description = any(), accountId = any(), onSent = any()) }
                        }
                    }
                }
            }
        }

        Given("고른 파일의 이름이나 크기를 읽을 수 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val uri = fixtureMonkey.fileUri()
            val exception = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.readSource(uri = uri) } throws exception
            val useCase = useCase(accountFlow = MutableStateFlow(Result.success(account)), fileRepository = fileRepository)

            When("그 파일을 올린다") {
                val resultList = useCase(parameter = UploadFileRequest(content = content(uri = uri), accountId = account.id)).toList()

                Then("TC-FILE-STORAGE-DOMAIN-003 서버에 요청하지 않고 크기 초과가 아닌 실패로 끝난다") {
                    // 채널 흐름을 건너며 코루틴이 예외를 복제할 수 있으므로 같은 종류와 내용인지로 확인한다.
                    val failure = resultList.last().shouldBeFailure()
                    failure.shouldNotBeInstanceOf<FileTooLargeException>()
                    failure.shouldBeInstanceOf<IllegalStateException>().message shouldBe exception.message
                    coVerify(exactly = 0) { fileRepository.create(source = any(), title = any(), description = any(), accountId = any(), onSent = any()) }
                }
            }
        }

        Given("올리는 도중 고른 파일의 내용을 읽을 수 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val source = fixtureMonkey.fileUploadSource()
            val exception = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.readSource(uri = source.uri) } returns source
            coEvery { fileRepository.create(source = source, title = any(), description = any(), accountId = any(), onSent = any()) } throws exception
            val useCase = useCase(accountFlow = MutableStateFlow(Result.success(account)), fileRepository = fileRepository)

            When("그 파일을 올린다") {
                val resultList = useCase(parameter = UploadFileRequest(content = content(uri = source.uri), accountId = account.id)).toList()

                Then("TC-FILE-STORAGE-DOMAIN-008 크기 초과가 아닌 실패로 끝난다") {
                    // 채널 흐름을 건너며 코루틴이 예외를 복제할 수 있으므로 같은 종류와 내용인지로 확인한다.
                    val failure = resultList.last().shouldBeFailure()
                    failure.shouldNotBeInstanceOf<FileTooLargeException>()
                    failure.shouldBeInstanceOf<IllegalStateException>().message shouldBe exception.message
                }
            }
        }

        Given("계정 A로 파일을 올리는 중이다") {
            When("계정이 게스트나 다른 계정으로 바뀐다") {
                Then("TC-FILE-STORAGE-DOMAIN-010 올리기를 중단하고 계정이 바뀌었다는 실패로 끝난다") {
                    listOf<Account>(Account.Guest, fixtureMonkey.giveMeOne<Account.User>()).forEach { changedAccount ->
                        runTest {
                            val account = fixtureMonkey.giveMeOne<Account.User>()
                            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
                            val isCancelled = CompletableDeferred<Unit>()
                            val source = fixtureMonkey.fileUploadSource()
                            val fileRepository = mockk<FileRepository>()
                            coEvery { fileRepository.readSource(uri = source.uri) } returns source
                            coEvery { fileRepository.create(source = source, title = any(), description = any(), accountId = any(), onSent = any()) } coAnswers {
                                try {
                                    awaitCancellation()
                                } finally {
                                    isCancelled.complete(Unit)
                                }
                            }
                            val useCase = useCase(accountFlow = accountFlow, fileRepository = fileRepository)

                            val result = async { useCase(parameter = UploadFileRequest(content = content(uri = source.uri), accountId = account.id)).toList() }
                            runCurrent()
                            accountFlow.value = Result.success(changedAccount)

                            val resultList = result.await()
                            resultList.map { item -> item.getOrNull() } shouldBe listOf(FileUploadStep.Started(source = source), null)
                            resultList.last().shouldBeFailure().shouldBeInstanceOf<FileUploadAccountChangedException>()
                            isCancelled.isCompleted shouldBe true
                        }
                    }
                }
            }

            When("같은 계정의 세션 갱신 여부만 바뀌거나 계정을 잠시 확인하지 못한다") {
                Then("TC-FILE-STORAGE-DOMAIN-010 TC-FILE-HOME-DOMAIN-009 올리기가 이어지고 서버가 응답하면 그 결과가 나온다") {
                    listOf<(Account.User) -> Result<Account>>(
                        { account -> Result.success(account.copy(isSessionValid = !account.isSessionValid)) },
                        { Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())) },
                    ).forEach { change ->
                        runTest {
                            val account = fixtureMonkey.giveMeOne<Account.User>()
                            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
                            val source = fixtureMonkey.fileUploadSource()
                            val file = fixtureMonkey.giveMeOne<DiaryFile>()
                            val completion = CompletableDeferred<DiaryFile>()
                            val fileRepository = mockk<FileRepository>()
                            coEvery { fileRepository.readSource(uri = source.uri) } returns source
                            coEvery { fileRepository.create(source = source, title = any(), description = any(), accountId = any(), onSent = any()) } coAnswers { completion.await() }
                            val useCase = useCase(accountFlow = accountFlow, fileRepository = fileRepository)

                            val result = async { useCase(parameter = UploadFileRequest(content = content(uri = source.uri), accountId = account.id)).toList() }
                            runCurrent()
                            accountFlow.value = change(account)
                            runCurrent()
                            completion.complete(file)

                            result.await().last().getOrThrow() shouldBe FileUploadStep.Completed(source = source, file = file)
                        }
                    }
                }
            }
        }

        Given("올리기를 시작한 계정이 이미 현재 계정이 아니다") {
            val source = fixtureMonkey.fileUploadSource()
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.readSource(uri = source.uri) } coAnswers { awaitCancellation() }
            val useCase = useCase(accountFlow = MutableStateFlow(Result.success(Account.Guest)), fileRepository = fileRepository)

            When("그 계정으로 올리기를 시작한다") {
                val resultList = useCase(parameter = UploadFileRequest(content = content(uri = source.uri), accountId = fixtureMonkey.giveMeOne<Uuid>())).toList()

                Then("TC-FILE-STORAGE-DOMAIN-010 서버에 보내지 않고 곧바로 중단한다") {
                    resultList.last().shouldBeFailure().shouldBeInstanceOf<FileUploadAccountChangedException>()
                    coVerify(exactly = 0) { fileRepository.create(source = any(), title = any(), description = any(), accountId = any(), onSent = any()) }
                }
            }
        }

        Given("사용자가 제목과 설명을 적어 파일 올리기를 시작했다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val source = fixtureMonkey.fileUploadSource()
            val content = content(uri = source.uri)
            val file = fixtureMonkey.giveMeOne<DiaryFile>()
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.readSource(uri = source.uri) } returns source
            coEvery { fileRepository.create(source = source, title = content.title, description = content.description, accountId = account.id, onSent = any()) } returns file
            val useCase = useCase(accountFlow = MutableStateFlow(Result.success(account)), fileRepository = fileRepository)

            When("그 파일을 올린다") {
                val last = useCase(parameter = UploadFileRequest(content = content, accountId = account.id)).toList().last()

                Then("TC-FILE-STORAGE-DOMAIN-016 적은 제목과 설명을 그대로 함께 올린다") {
                    last.getOrThrow() shouldBe FileUploadStep.Completed(source = source, file = file)
                    coVerify(exactly = 1) { fileRepository.create(source = source, title = content.title, description = content.description, accountId = account.id, onSent = any()) }
                }
            }
        }

        Given("고를 때 1,024바이트였던 파일이 올리기를 시작할 때는 52,428,801바이트다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val source = fixtureMonkey.fileUploadSource(size = MAX_SIZE + 1)
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.readSource(uri = source.uri) } returns source
            val useCase = useCase(accountFlow = MutableStateFlow(Result.success(account)), fileRepository = fileRepository)

            When("그 파일을 올린다") {
                val last = useCase(parameter = UploadFileRequest(content = content(uri = source.uri), accountId = account.id)).toList().last()

                Then("TC-FILE-ADD-DOMAIN-001 다시 확인해 서버에 요청하지 않고 크기 초과로 실패한다") {
                    last.shouldBeFailure().shouldBeInstanceOf<FileTooLargeException>()
                    coVerify(exactly = 0) { fileRepository.create(source = any(), title = any(), description = any(), accountId = any(), onSent = any()) }
                }
            }
        }
    }) {
    public companion object {
        private fun content(uri: FileUri): FileUploadContent =
            FileUploadContent(
                uri = uri,
                title = "title-${fixtureMonkey.giveMeOne<String>()}",
                description = fixtureMonkey.giveMeOne<String>(),
            )

        private fun useCase(
            accountFlow: MutableStateFlow<Result<Account>>,
            fileRepository: FileRepository,
        ): UploadFileUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow

            return UploadFileUseCase(
                awaitFileUploadAccountChangeUseCase = AwaitFileUploadAccountChangeUseCase(getAccountUseCase = getAccountUseCase),
                fileRepository = fileRepository,
            )
        }
    }
}
