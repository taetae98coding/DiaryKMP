package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoFilterRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SelectMemoFilterTagUseCaseTest :
    BehaviorSpec({
        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoFilterRepository = mockk<AccountMemoFilterRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            coEvery { accountMemoFilterRepository.upsert(account = account, tagId = tagId) } just Runs
            val useCase =
                SelectMemoFilterTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoFilterRepository = accountMemoFilterRepository,
                )

            When("태그를 필터에 선택한다") {
                Then("현재 계정의 필터 선택으로 저장한다") {
                    useCase(parameter = tagId).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        accountMemoFilterRepository.upsert(
                            account = account,
                            tagId = tagId,
                        )
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoFilterRepository = mockk<AccountMemoFilterRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase =
                SelectMemoFilterTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoFilterRepository = accountMemoFilterRepository,
                )

            When("태그를 필터에 선택한다") {
                Then("계정 조회 실패를 전달하고 저장소에 선택을 요청하지 않는다") {
                    useCase(parameter = tagId).shouldBeFailure { throwable -> throwable shouldBe failure }

                    coVerify(exactly = 0) {
                        accountMemoFilterRepository.upsert(account = any(), tagId = any())
                    }
                }
            }
        }
    })
