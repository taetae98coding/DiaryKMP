package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.file.fileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class RequestFileUploadUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정이 파일을 골랐다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val uri = fixtureMonkey.fileUri()
            val fileUploadManager = mockk<FileUploadManager>(relaxUnitFun = true)
            val useCase = RequestFileUploadUseCase(getAccountUseCase = getAccountUseCase(account = account), fileUploadManager = fileUploadManager)

            When("올리기를 요청한다") {
                val result = useCase(parameter = uri)

                Then("그 계정으로 고른 파일의 올리기를 맡긴다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { fileUploadManager.requestUpload(uri = uri, accountId = account.id) }
                }
            }
        }

        Given("게스트 상태다") {
            val fileUploadManager = mockk<FileUploadManager>(relaxUnitFun = true)
            val useCase = RequestFileUploadUseCase(getAccountUseCase = getAccountUseCase(account = Account.Guest), fileUploadManager = fileUploadManager)

            When("올리기를 요청한다") {
                val result = useCase(parameter = fixtureMonkey.fileUri())

                Then("올리기를 맡기지 않고 실패로 끝난다") {
                    result.shouldBeFailure()
                    coVerify(exactly = 0) { fileUploadManager.requestUpload(uri = any(), accountId = any()) }
                }
            }
        }
    }) {
    public companion object {
        private fun getAccountUseCase(account: Account): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(Result.success(account))
            return useCase
        }
    }
}
