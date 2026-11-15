package io.github.taetae98coding.diary.domain.routine.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.calendarRoutineRecord
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.repository.AccountCalendarRoutineRecordRepository
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
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

class GetCalendarRoutineRecordUseCaseTest :
    BehaviorSpec({
        Given("TC-06ca270a-c2c9-4e76-bbd7-83d845d510fd TC-31835a2a-74c5-4312-9179-40db74187d4f: 두 계정에 각각 루틴 기록이 있고 첫 번째 계정으로 결과를 계속 관찰하고 있다") {
            val dateRange = Arb.localDateRange().next()
            val account = Arb.accountUser().next()
            val otherAccount = Arb.accountUser().next()
            val recordList = listOf(Arb.calendarRoutineRecord().next())
            val otherRecordList = listOf(Arb.calendarRoutineRecord().next())
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns accountFlow
            val repository = mockk<AccountCalendarRoutineRecordRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(recordList)
            every { repository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherRecordList)
            val useCase = GetCalendarRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountCalendarRoutineRecordRepository = repository)

            When("현재 사용자 계정이 두 번째 계정으로 바뀐다") {
                Then("두 번째 계정의 루틴 기록만 담긴 결과가 이어서 전달된다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe recordList

                        accountFlow.value = Result.success(otherAccount)

                        awaitItem().shouldBeSuccess() shouldBe otherRecordList
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("TC-32e3c1eb-b9d8-493d-99c7-894aa39f1b9f: 표시 대상 기간에 드는 루틴 기록이 없다") {
            val dateRange = Arb.localDateRange().next()
            val account = Arb.accountUser().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarRoutineRecordRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(emptyList())
            val useCase = GetCalendarRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountCalendarRoutineRecordRepository = repository)

            When("표시 대상 기간으로 캘린더 루틴 기록을 조회한다") {
                Then("조회가 성공하고 결과는 비어 있다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess().shouldBeEmpty()
                        awaitComplete()
                    }
                }
            }
        }

        Given("TC-dc0abe02-1923-4800-bd58-e16cb4cdf229: 기기에서 루틴 기록의 조회가 실패한다") {
            val dateRange = Arb.localDateRange().next()
            val account = Arb.accountUser().next()
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarRoutineRecordRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flow { throw failure }
            val useCase = GetCalendarRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountCalendarRoutineRecordRepository = repository)

            When("표시 대상 기간으로 캘린더 루틴 기록을 조회한다") {
                Then("빈 결과가 아니라 실패한 결과가 전달된다") {
                    useCase(parameter = dateRange)
                        .first()
                        .shouldBeFailure()
                        .shouldBeInstanceOf<IllegalStateException>()
                        .message shouldBe failure.message
                }
            }
        }

        Given("현재 계정 조회가 실패한다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val repository = mockk<AccountCalendarRoutineRecordRepository>(relaxed = true)
            val useCase = GetCalendarRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountCalendarRoutineRecordRepository = repository)

            When("표시 대상 기간으로 캘린더 루틴 기록을 조회한다") {
                Then("TC-dc0abe02-1923-4800-bd58-e16cb4cdf229 계정 조회의 실패를 그대로 전달하고 기록을 조회하지 않는다") {
                    useCase(parameter = Arb.localDateRange().next()).test {
                        awaitItem().shouldBeFailure() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.get(account = any(), dateRange = any()) }
                }
            }
        }
    })
