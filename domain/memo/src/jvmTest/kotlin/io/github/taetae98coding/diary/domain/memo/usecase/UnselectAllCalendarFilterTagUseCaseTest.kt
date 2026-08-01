package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarFilterRepository
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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class UnselectAllCalendarFilterTagUseCaseTest :
    BehaviorSpec({
        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountCalendarFilterRepository = mockk<AccountCalendarFilterRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            coEvery { accountCalendarFilterRepository.deleteAll(account = account) } just Runs
            val useCase =
                UnselectAllCalendarFilterTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarFilterRepository = accountCalendarFilterRepository,
                )

            When("태그 필터 선택을 전체 해제한다") {
                Then("현재 계정의 필터 선택을 모두 제거한다") {
                    useCase(parameter = Unit).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        accountCalendarFilterRepository.deleteAll(account = account)
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountCalendarFilterRepository = mockk<AccountCalendarFilterRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase =
                UnselectAllCalendarFilterTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarFilterRepository = accountCalendarFilterRepository,
                )

            When("태그 필터 선택을 전체 해제한다") {
                Then("계정 조회 실패를 전달하고 저장소에 해제를 요청하지 않는다") {
                    useCase(parameter = Unit).shouldBeFailure { throwable -> throwable shouldBe failure }

                    coVerify(exactly = 0) {
                        accountCalendarFilterRepository.deleteAll(account = any())
                    }
                }
            }
        }
    })
