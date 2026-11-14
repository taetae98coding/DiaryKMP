package io.github.taetae98coding.diary.domain.dday.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class DDayHomePageDDayUseCaseTest :
    BehaviorSpec({
        Given("현재 계정의 디데이 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val pagingData = PagingData.from(listOf(Arb.dday(isDeleted = false).next()))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountDDayRepository>()
            every { repository.page(account = account) } returns flowOf(pagingData)
            val useCase = PageDDayUseCase(getAccountUseCase = getAccountUseCase, accountDDayRepository = repository)

            When("디데이 목록을 조회한다") {
                Then("현재 계정의 디데이 페이지를 반환한다") {
                    useCase().test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정 조회가 실패하도록 설정되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val repository = mockk<AccountDDayRepository>(relaxed = true)
            val useCase = PageDDayUseCase(getAccountUseCase = getAccountUseCase, accountDDayRepository = repository)

            When("디데이 목록을 조회한다") {
                Then("TC-22f92e42-b828-4b15-ac9f-7447050f4090 노출하는 디데이 없이 계정 조회의 실패를 그대로 전달한다") {
                    useCase().test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.page(account = any()) }
                }
            }
        }
    })
