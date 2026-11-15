package io.github.taetae98coding.diary.domain.dday.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.dday.calendarDDay
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.repository.AccountCalendarDDayRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

class GetCalendarDDayUseCaseTest :
    BehaviorSpec({
        Given("TC-49b0c2cf-9276-494f-b833-6cdc468f5301: 두 계정에 각각 디데이가 있고 첫 번째 계정으로 결과를 계속 관찰하고 있다") {
            val dateRange = dateRange()
            val account = Arb.accountUser().next()
            val otherAccount = Arb.accountUser().next()
            val ddayList = listOf(Arb.calendarDDay().next())
            val otherDDayList = listOf(Arb.calendarDDay().next())
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns accountFlow
            val repository = mockk<AccountCalendarDDayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(ddayList)
            every { repository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherDDayList)
            val useCase = GetCalendarDDayUseCase(getAccountUseCase = getAccountUseCase, accountCalendarDDayRepository = repository)

            When("현재 사용자 계정이 두 번째 계정으로 바뀐다") {
                Then("두 번째 계정의 디데이만 담긴 결과가 이어서 전달된다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe ddayList

                        accountFlow.value = Result.success(otherAccount)

                        awaitItem().shouldBeSuccess() shouldBe otherDDayList
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("TC-dfea8cb8-9a9a-416a-befc-d9005745328b: 표시 대상 기간에 드는 디데이가 없다") {
            val dateRange = dateRange()
            val account = Arb.accountUser().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarDDayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(emptyList())
            val useCase = GetCalendarDDayUseCase(getAccountUseCase = getAccountUseCase, accountCalendarDDayRepository = repository)

            When("표시 대상 기간으로 캘린더 디데이를 조회한다") {
                Then("조회가 성공하고 결과는 비어 있다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe emptyList()
                }
            }
        }

        Given("TC-5c78bfb1-e6ea-46ff-b06a-97d2be8d8d0a: 기기에서 디데이 조회가 실패하도록 준비되어 있다") {
            val dateRange = dateRange()
            val account = Arb.accountUser().next()
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarDDayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flow { throw failure }
            val useCase = GetCalendarDDayUseCase(getAccountUseCase = getAccountUseCase, accountCalendarDDayRepository = repository)

            When("표시 대상 기간으로 캘린더 디데이를 조회한다") {
                Then("빈 결과가 아니라 실패한 결과가 전달된다") {
                    useCase(parameter = dateRange)
                        .first()
                        .shouldBeFailure()
                        .shouldBeInstanceOf<IllegalStateException>()
                        .message shouldBe failure.message
                }
            }
        }

        Given("현재 사용자 계정 확인이 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val repository = mockk<AccountCalendarDDayRepository>(relaxed = true)
            val useCase = GetCalendarDDayUseCase(getAccountUseCase = getAccountUseCase, accountCalendarDDayRepository = repository)

            When("표시 대상 기간으로 캘린더 디데이를 조회한다") {
                Then("실패한 결과가 전달되고 디데이를 조회하지 않는다") {
                    useCase(parameter = dateRange()).first().shouldBeFailure() shouldBe failure

                    verify(exactly = 0) { repository.get(account = any(), dateRange = any()) }
                }
            }
        }
    })

private fun dateRange(): LocalDateRange = LocalDateRange(LocalDate(2026, 6, 28), LocalDate(2026, 8, 8))
