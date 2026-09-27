package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import io.github.taetae98coding.diary.domain.file.exception.FileNotSelectedException
import io.github.taetae98coding.diary.domain.file.exception.FileTitleBlankException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class RequestFileUploadUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정이 제목과 설명을 적고 파일을 골랐다") {
            When("올리기를 요청한다") {
                Then("TC-FILE-ADD-DOMAIN-002 그 계정으로 고른 파일을 적은 제목과 설명 그대로 올리도록 맡긴다") {
                    listOf(
                        " 회의록 " to "첫 줄\n둘째 줄",
                        "회의록" to "",
                    ).forEach { (title, description) ->
                        val account = fixtureMonkey.giveMeOne<Account.User>()
                        val uri = fixtureMonkey.fileUri()
                        val fileUploadManager = mockk<FileUploadManager>(relaxUnitFun = true)
                        val useCase = RequestFileUploadUseCase(getAccountUseCase = getAccountUseCase(result = Result.success(account)), fileUploadManager = fileUploadManager)

                        val result = useCase(parameter = RequestFileUploadUseCase.Parameter(uri = uri, title = title, description = description))

                        result.shouldBeSuccess()
                        coVerify(exactly = 1) {
                            fileUploadManager.requestUpload(
                                content = FileUploadContent(uri = uri, title = title, description = description),
                                accountId = account.id,
                            )
                        }
                    }
                }
            }
        }

        Given("제목이 비어 있거나 공백만 있다") {
            When("파일을 고른 채 올리기를 요청한다") {
                Then("TC-FILE-ADD-FEATURE-006 제목이 없다는 실패로 끝나고 올리기를 맡기지 않는다") {
                    listOf("", "  ").forEach { title ->
                        val fileUploadManager = mockk<FileUploadManager>(relaxUnitFun = true)
                        val useCase = useCase(fileUploadManager = fileUploadManager)

                        val result = useCase(parameter = RequestFileUploadUseCase.Parameter(uri = fixtureMonkey.fileUri(), title = title, description = fixtureMonkey.giveMeOne<String>()))

                        result.shouldBeFailure().shouldBeInstanceOf<FileTitleBlankException>()
                        coVerify(exactly = 0) { fileUploadManager.requestUpload(content = any(), accountId = any()) }
                    }
                }
            }

            When("파일도 고르지 않은 채 올리기를 요청한다") {
                val fileUploadManager = mockk<FileUploadManager>(relaxUnitFun = true)
                val result = useCase(fileUploadManager = fileUploadManager)(parameter = RequestFileUploadUseCase.Parameter(uri = null, title = "", description = ""))

                Then("TC-FILE-ADD-FEATURE-008 파일보다 제목이 없다는 실패를 먼저 알린다") {
                    result.shouldBeFailure().shouldBeInstanceOf<FileTitleBlankException>()
                }
            }
        }

        Given("제목을 적었지만 고른 파일이 없다") {
            val fileUploadManager = mockk<FileUploadManager>(relaxUnitFun = true)

            When("올리기를 요청한다") {
                val result = useCase(fileUploadManager = fileUploadManager)(parameter = RequestFileUploadUseCase.Parameter(uri = null, title = "회의록", description = ""))

                Then("TC-FILE-ADD-FEATURE-007 파일을 고르지 않았다는 실패로 끝나고 올리기를 맡기지 않는다") {
                    result.shouldBeFailure().shouldBeInstanceOf<FileNotSelectedException>()
                    coVerify(exactly = 0) { fileUploadManager.requestUpload(content = any(), accountId = any()) }
                }
            }
        }

        Given("계정 상태가 게스트이거나 확정되지 않았다") {
            When("제목을 적고 파일을 고른 채 올리기를 요청한다") {
                Then("TC-FILE-ADD-FEATURE-016 올리기를 맡기지 않고 실패로 끝난다") {
                    listOf<Result<Account>>(
                        Result.success(Account.Guest),
                        Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                    ).forEach { accountResult ->
                        val fileUploadManager = mockk<FileUploadManager>(relaxUnitFun = true)
                        val useCase = RequestFileUploadUseCase(getAccountUseCase = getAccountUseCase(result = accountResult), fileUploadManager = fileUploadManager)

                        val result = useCase(parameter = RequestFileUploadUseCase.Parameter(uri = fixtureMonkey.fileUri(), title = "회의록", description = ""))

                        result.shouldBeFailure()
                        coVerify(exactly = 0) { fileUploadManager.requestUpload(content = any(), accountId = any()) }
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun useCase(fileUploadManager: FileUploadManager): RequestFileUploadUseCase =
            RequestFileUploadUseCase(
                getAccountUseCase = getAccountUseCase(result = Result.success(fixtureMonkey.giveMeOne<Account.User>())),
                fileUploadManager = fileUploadManager,
            )

        private fun getAccountUseCase(result: Result<Account>): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(result)
            return useCase
        }
    }
}
