package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class UploadFileUseCaseTest :
    BehaviorSpec({
        Given("고른 파일을 서버에 보관할 수 있다") {
            val uri = fixtureMonkey.fileUri()
            val file = fixtureMonkey.giveMeOne<DiaryFile>()
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.create(uri = uri, maxSize = any()) } returns file
            val useCase = UploadFileUseCase(getAccountUseCase = getAccountUseCase(), fileRepository = fileRepository)

            When("그 파일을 올린다") {
                val result = useCase(parameter = uri)

                Then("TC-FILE-STORAGE-DOMAIN-001 50MB를 올릴 수 있는 최대 크기로 정해 보관하고 보관한 파일을 전달한다") {
                    result.shouldBeSuccess() shouldBe file
                    coVerify(exactly = 1) { fileRepository.create(uri = uri, maxSize = 52_428_800) }
                }
            }
        }

        Given("고른 파일이 올릴 수 있는 크기를 넘는다") {
            val uri = fixtureMonkey.fileUri()
            val exception = FileTooLargeException(message = "too large")
            val fileRepository = mockk<FileRepository>()
            coEvery { fileRepository.create(uri = uri, maxSize = any()) } throws exception
            val useCase = UploadFileUseCase(getAccountUseCase = getAccountUseCase(), fileRepository = fileRepository)

            When("그 파일을 올린다") {
                val result = useCase(parameter = uri)

                Then("TC-FILE-STORAGE-DOMAIN-001 크기 초과 실패를 그대로 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(exception)
                }
            }
        }

        Given("계정 A로 파일을 올리는 중이다") {
            When("계정이 테스트 데이터의 상태로 바뀐다") {
                Then("TC-FILE-HOME-FEATURE-024 올리기를 중단하고 계정이 바뀌었다는 실패로 끝난다") {
                    listOf<Account>(Account.Guest, fixtureMonkey.giveMeOne<Account.User>()).forEach { changedAccount ->
                        runTest {
                            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(fixtureMonkey.giveMeOne<Account.User>()))
                            val isCancelled = CompletableDeferred<Unit>()
                            val fileRepository = mockk<FileRepository>()
                            coEvery { fileRepository.create(uri = any(), maxSize = any()) } coAnswers {
                                try {
                                    awaitCancellation()
                                } finally {
                                    isCancelled.complete(Unit)
                                }
                            }
                            val useCase = UploadFileUseCase(getAccountUseCase = getAccountUseCase(accountFlow = accountFlow), fileRepository = fileRepository)

                            val result = async { useCase(parameter = fixtureMonkey.fileUri()) }
                            runCurrent()
                            accountFlow.value = Result.success(changedAccount)

                            result.await().shouldBeFailure().shouldBeInstanceOf<FileUploadAccountChangedException>()
                            isCancelled.isCompleted shouldBe true
                        }
                    }
                }
            }

            When("같은 계정의 세션 갱신 여부만 바뀐다") {
                Then("올리기를 계속한다") {
                    runTest {
                        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                        val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
                        val file = fixtureMonkey.giveMeOne<DiaryFile>()
                        val completion = CompletableDeferred<DiaryFile>()
                        val fileRepository = mockk<FileRepository>()
                        coEvery { fileRepository.create(uri = any(), maxSize = any()) } coAnswers { completion.await() }
                        val useCase = UploadFileUseCase(getAccountUseCase = getAccountUseCase(accountFlow = accountFlow), fileRepository = fileRepository)

                        val result = async { useCase(parameter = fixtureMonkey.fileUri()) }
                        runCurrent()
                        accountFlow.value = Result.success(account.copy(isSessionValid = false))
                        runCurrent()
                        completion.complete(file)

                        result.await().shouldBeSuccess() shouldBe file
                    }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun getAccountUseCase(accountFlow: MutableStateFlow<Result<Account>> = MutableStateFlow(Result.success(fixtureMonkey.giveMeOne<Account.User>()))): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns accountFlow
            return useCase
        }
    }
}
