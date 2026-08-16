package io.github.taetae98coding.diary.domain.contact.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountCalendarContactBirthdayRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetCalendarContactBirthdayUseCaseTest :
    BehaviorSpec({
        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-004: 두 계정에 각각 다른 연락처가 저장되어 있고 현재 계정은 첫 번째 계정이다") {
            val dateRange = dateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val birthdayList = listOf(calendarContactBirthday(), calendarContactBirthday())
            val otherBirthdayList = listOf(calendarContactBirthday())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarContactBirthdayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(birthdayList)
            every { repository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherBirthdayList)
            val useCase =
                GetCalendarContactBirthdayUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarContactBirthdayRepository = repository,
                )

            When("표시 대상 기간으로 캘린더 생일을 조회한다") {
                Then("첫 번째 계정과 연결된 연락처의 생일만 결과에 포함된다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe birthdayList

                    verify(exactly = 1) { repository.get(account = account, dateRange = dateRange) }
                    verify(exactly = 0) { repository.get(account = otherAccount, dateRange = any()) }
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-012: 두 계정에 각각 생일이 저장되어 있고 첫 번째 계정으로 조회해 결과를 계속 관찰하고 있다") {
            val dateRange = dateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val birthdayList = listOf(calendarContactBirthday())
            val otherBirthdayList = listOf(calendarContactBirthday(), calendarContactBirthday())
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val repository = mockk<AccountCalendarContactBirthdayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(birthdayList)
            every { repository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherBirthdayList)
            val useCase =
                GetCalendarContactBirthdayUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarContactBirthdayRepository = repository,
                )

            When("현재 사용자 계정이 두 번째 계정으로 바뀐다") {
                Then("두 번째 계정과 연결된 연락처의 생일만 담긴 결과가 이어서 전달된다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe birthdayList

                        accountFlow.value = Result.success(otherAccount)

                        awaitItem().shouldBeSuccess() shouldBe otherBirthdayList
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-013: 표시 대상 기간에 드는 생일이 없다") {
            val dateRange = dateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarContactBirthdayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(emptyList())
            val useCase =
                GetCalendarContactBirthdayUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarContactBirthdayRepository = repository,
                )

            When("표시 대상 기간으로 캘린더 생일을 조회한다") {
                Then("조회가 성공하고 결과는 비어 있다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe emptyList()
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-014: 캘린더 생일 조회가 실패하도록 준비되어 있다") {
            val dateRange = dateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarContactBirthdayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flow { throw failure }
            val useCase =
                GetCalendarContactBirthdayUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarContactBirthdayRepository = repository,
                )

            When("표시 대상 기간으로 캘린더 생일을 조회한다") {
                Then("실패한 결과가 전달된다") {
                    useCase(parameter = dateRange)
                        .first()
                        .shouldBeFailure()
                        .shouldBeInstanceOf<IllegalStateException>()
                        .message shouldBe failure.message
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-015: 현재 사용자 계정 확인이 실패하도록 준비되어 있다") {
            val dateRange = dateRange()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val repository = mockk<AccountCalendarContactBirthdayRepository>(relaxed = true)
            val useCase =
                GetCalendarContactBirthdayUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarContactBirthdayRepository = repository,
                )

            When("표시 대상 기간으로 캘린더 생일을 조회한다") {
                Then("실패한 결과가 전달되고 생일을 조회하지 않는다") {
                    useCase(parameter = dateRange).first().shouldBeFailure() shouldBeSameInstanceAs failure

                    verify(exactly = 0) { repository.get(account = any(), dateRange = any()) }
                }
            }
        }
    }) {
    public companion object {
        private fun dateRange(): LocalDateRange = LocalDateRange(LocalDate(2026, 6, 28), LocalDate(2026, 8, 8))

        private fun calendarContactBirthday(): CalendarContactBirthday =
            fixtureMonkey
                .giveMeKotlinBuilder<CalendarContactBirthday>()
                .setExp(CalendarContactBirthday::date, LocalDate(2026, 7, 8))
                .sample()
    }
}
