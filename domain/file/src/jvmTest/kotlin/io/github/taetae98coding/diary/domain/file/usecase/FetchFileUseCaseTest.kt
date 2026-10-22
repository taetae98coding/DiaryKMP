package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchFileUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정이다") {
            When("파일 정보를 불러온다") {
                Then("TC-a3f4dfdb-e1bd-45c2-9cfc-36710034083e 그 식별자로 서버에서 받은 정보를 돌려준다") {
                    val id = fixtureMonkey.giveMeOne<Uuid>()
                    val file = fixtureMonkey.giveMeOne<DiaryFile>()
                    val fileRepository = mockk<FileDetailRepository>()
                    coEvery { fileRepository.fetch(id = id) } returns file
                    val useCase = FetchFileUseCase(getAccountUseCase = getAccountUseCase(Result.success(fixtureMonkey.giveMeOne<Account.User>())), fileDetailRepository = fileRepository)

                    useCase(parameter = id).shouldBeSuccess() shouldBe file
                    coVerify(exactly = 1) { fileRepository.fetch(id = id) }
                }
            }
        }

        Given("계정을 확인하지 못했다") {
            When("파일 정보를 불러온다") {
                Then("서버에 요청하지 않고 실패로 끝난다") {
                    val fileRepository = mockk<FileDetailRepository>()
                    val useCase = FetchFileUseCase(getAccountUseCase = getAccountUseCase(Result.failure(IllegalStateException("account"))), fileDetailRepository = fileRepository)

                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>()).shouldBeFailure()
                    coVerify(exactly = 0) { fileRepository.fetch(id = any()) }
                }
            }
        }
    })

private fun getAccountUseCase(result: Result<Account>): GetAccountUseCase = mockk<GetAccountUseCase>().also { useCase -> every { useCase(parameter = Unit) } returns flowOf(result) }
