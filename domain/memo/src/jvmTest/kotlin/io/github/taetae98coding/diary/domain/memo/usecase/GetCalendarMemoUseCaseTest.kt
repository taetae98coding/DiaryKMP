package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarMemoRepository
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetCalendarMemoUseCaseTest :
    BehaviorSpec({
        Given("TC-CALENDAR-MEMO-DOMAIN-006: 두 계정에 각각 다른 메모가 저장되어 있고 현재 계정은 첫 번째 계정이다") {
            val dateRange = randomDateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val memoList = listOf(calendarMemo(), calendarMemo())
            val otherMemoList = listOf(calendarMemo())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>()
            every { accountCalendarMemoRepository.get(account = account, dateRange = dateRange) } returns flowOf(memoList)
            every { accountCalendarMemoRepository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherMemoList)
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("표시 대상 기간으로 캘린더 메모를 조회한다") {
                Then("첫 번째 계정과 연결된 메모만 결과에 포함된다") {
                    val result = useCase(parameter = dateRange).first()

                    result.shouldBeSuccess() shouldBe memoList
                    verify(exactly = 1) {
                        accountCalendarMemoRepository.get(account = account, dateRange = dateRange)
                    }
                    verify(exactly = 0) {
                        accountCalendarMemoRepository.get(account = otherAccount, dateRange = any())
                    }
                }
            }
        }

        Given("TC-CALENDAR-MEMO-DOMAIN-009: 서로 겹치지 않는 두 기간에 각각 다른 메모가 저장되어 있다") {
            val dateRange = randomDateRange()
            val otherDateRange = nextDateRange(dateRange)
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoList = listOf(calendarMemo(), calendarMemo())
            val otherMemoList = listOf(calendarMemo())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>()
            every { accountCalendarMemoRepository.get(account = account, dateRange = dateRange) } returns flowOf(memoList)
            every { accountCalendarMemoRepository.get(account = account, dateRange = otherDateRange) } returns flowOf(otherMemoList)
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("첫 번째 기간으로 조회한 뒤 두 번째 기간으로 바꿔 조회한다") {
                Then("두 번째 기간에 저장된 메모만 결과에 포함된다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe memoList

                    val result = useCase(parameter = otherDateRange).first()

                    result.shouldBeSuccess() shouldBe otherMemoList
                }
            }
        }

        Given("TC-CALENDAR-MEMO-DOMAIN-010: 제목이 `기존 제목`인 메모를 표시 대상 기간으로 조회해 결과를 계속 관찰하고 있다") {
            val dateRange = randomDateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memo = calendarMemo().copy(title = "기존 제목 ${fixtureMonkey.giveMeOne<String>()}")
            val memoList = listOf(memo)
            val changedMemoList = listOf(memo.copy(title = "새 제목 ${fixtureMonkey.giveMeOne<String>()}"))
            val memoListFlow = MutableStateFlow(memoList)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>()
            every { accountCalendarMemoRepository.get(account = account, dateRange = dateRange) } returns memoListFlow
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("조회를 다시 요청하지 않은 상태에서 그 메모의 제목이 `새 제목`으로 바뀐다") {
                Then("같은 메모의 제목이 `새 제목`으로 바뀐 결과가 이어서 전달된다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe memoList

                        memoListFlow.value = changedMemoList

                        awaitItem().shouldBeSuccess() shouldBe changedMemoList
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("TC-CALENDAR-MEMO-DOMAIN-011: 두 계정에 각각 메모가 저장되어 있고 첫 번째 계정으로 조회해 결과를 계속 관찰하고 있다") {
            val dateRange = randomDateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val memoList = listOf(calendarMemo())
            val otherMemoList = listOf(calendarMemo(), calendarMemo())
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>()
            every { accountCalendarMemoRepository.get(account = account, dateRange = dateRange) } returns flowOf(memoList)
            every { accountCalendarMemoRepository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherMemoList)
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("현재 사용자 계정이 두 번째 계정으로 바뀐다") {
                Then("두 번째 계정과 연결된 메모만 담긴 결과가 이어서 전달된다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe memoList

                        accountFlow.value = Result.success(otherAccount)

                        awaitItem().shouldBeSuccess() shouldBe otherMemoList
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("TC-CALENDAR-MEMO-DOMAIN-012: 표시 대상 기간과 겹치는 메모가 없다") {
            val dateRange = randomDateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>()
            every { accountCalendarMemoRepository.get(account = account, dateRange = dateRange) } returns flowOf(emptyList())
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("표시 대상 기간으로 캘린더 메모를 조회한다") {
                Then("조회가 성공하고 결과는 비어 있다") {
                    val result = useCase(parameter = dateRange).first()

                    result.shouldBeSuccess() shouldBe emptyList()
                }
            }
        }

        Given("TC-CALENDAR-MEMO-DOMAIN-013: 캘린더 메모 조회가 실패하도록 준비되어 있다") {
            val dateRange = randomDateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>()
            every { accountCalendarMemoRepository.get(account = account, dateRange = dateRange) } returns flow { throw failure }
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("표시 대상 기간으로 캘린더 메모를 조회한다") {
                Then("실패한 결과가 전달된다") {
                    val result = useCase(parameter = dateRange).first()

                    result
                        .shouldBeFailure()
                        .shouldBeInstanceOf<IllegalStateException>()
                        .message shouldBe failure.message
                }
            }
        }

        Given("TC-CALENDAR-MEMO-DOMAIN-014: 현재 사용자 계정 확인이 실패하도록 준비되어 있다") {
            val dateRange = randomDateRange()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>(relaxed = true)
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("표시 대상 기간으로 캘린더 메모를 조회한다") {
                Then("실패한 결과가 전달되고 메모를 조회하지 않는다") {
                    val result = useCase(parameter = dateRange).first()

                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    verify(exactly = 0) {
                        accountCalendarMemoRepository.get(account = any(), dateRange = any())
                    }
                }
            }
        }

        Given("계정이 바뀐 뒤에도 이전 계정의 메모 변경이 이어지는 상황이 준비되어 있다") {
            val dateRange = randomDateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val memoList = listOf(calendarMemo())
            val changedMemoList = listOf(calendarMemo(), calendarMemo())
            val otherMemoList = listOf(calendarMemo())
            val changedOtherMemoList = listOf(calendarMemo(), calendarMemo(), calendarMemo())
            val memoListFlow = MutableStateFlow(memoList)
            val otherMemoListFlow = MutableStateFlow(otherMemoList)
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val accountCalendarMemoRepository = mockk<AccountCalendarMemoRepository>()
            every { accountCalendarMemoRepository.get(account = account, dateRange = dateRange) } returns memoListFlow
            every { accountCalendarMemoRepository.get(account = otherAccount, dateRange = dateRange) } returns otherMemoListFlow
            val useCase =
                GetCalendarMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountCalendarMemoRepository = accountCalendarMemoRepository,
                )

            When("계정이 바뀐 뒤 이전 계정의 메모와 현재 계정의 메모가 차례로 바뀐다") {
                Then("이전 계정의 메모 변경은 전달되지 않고 현재 계정의 변경만 전달된다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe memoList

                        accountFlow.value = Result.success(otherAccount)

                        awaitItem().shouldBeSuccess() shouldBe otherMemoList

                        memoListFlow.value = changedMemoList
                        otherMemoListFlow.value = changedOtherMemoList

                        awaitItem().shouldBeSuccess() shouldBe changedOtherMemoList
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    })

private fun calendarMemo(): CalendarMemo {
    val start = randomDate()

    return CalendarMemo(
        id = fixtureMonkey.giveMeOne(),
        title = fixtureMonkey.giveMeOne(),
        color = fixtureMonkey.giveMeOne(),
        dateTime =
            MemoDateTime.AllDay(dateRange = start..start),
    )
}

private fun randomDateRange(): LocalDateRange {
    val start = randomDate()

    return start..start.plus(6, DateTimeUnit.DAY)
}

private fun nextDateRange(dateRange: LocalDateRange): LocalDateRange {
    val start = dateRange.endInclusive.plus(1, DateTimeUnit.DAY)

    return start..start.plus(6, DateTimeUnit.DAY)
}

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
