package io.github.taetae98coding.diary.domain.contact.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.contact.LunarContactBirthday
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountCalendarContactBirthdayRepository
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetCalendarContactBirthdayUseCaseTest :
    BehaviorSpec({
        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-004: 두 계정에 각각 다른 연락처가 저장되어 있고 현재 계정은 첫 번째 계정이다") {
            val dateRange = dateRange()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val birthdayList = listOf(calendarContactBirthday(), calendarContactBirthday()).sortedForCalendar()
            val otherBirthdayList = listOf(calendarContactBirthday())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val repository = repository()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(birthdayList)
            every { repository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherBirthdayList)
            val useCase = useCase(getAccountUseCase = getAccountUseCase, repository = repository)

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
            val otherBirthdayList = listOf(calendarContactBirthday(), calendarContactBirthday()).sortedForCalendar()
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val repository = repository()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(birthdayList)
            every { repository.get(account = otherAccount, dateRange = dateRange) } returns flowOf(otherBirthdayList)
            val useCase = useCase(getAccountUseCase = getAccountUseCase, repository = repository)

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
            val repository = repository()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(emptyList())
            val useCase = useCase(getAccountUseCase = getAccountUseCase, repository = repository)

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
            val repository = repository()
            every { repository.get(account = account, dateRange = dateRange) } returns flow { throw failure }
            val useCase = useCase(getAccountUseCase = getAccountUseCase, repository = repository)

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
            val useCase = useCase(getAccountUseCase = getAccountUseCase, repository = repository)

            When("표시 대상 기간으로 캘린더 생일을 조회한다") {
                Then("실패한 결과가 전달되고 생일을 조회하지 않는다") {
                    useCase(parameter = dateRange).first().shouldBeFailure() shouldBeSameInstanceAs failure

                    verify(exactly = 0) { repository.get(account = any(), dateRange = any()) }
                    verify(exactly = 0) { repository.getLunar(account = any()) }
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-016: 저장된 날짜가 모두 1990년 7월 8일인 양력 생일 연락처와 음력 생일 연락처가 있고 2026년·2027년 음력 자료가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val solarBirthday = calendarContactBirthday(name = "양력", date = LocalDate(2026, 7, 8))
            val lunarBirthday = lunarContactBirthday(name = "음력", birthday = LocalDate(1990, 7, 8))
            val caseList =
                listOf(
                    // 표시 대상 기간, 그 기간의 양력 생일 조회 결과, 그 기간의 음력 자료, 기대 결과
                    Case(
                        dateRange = LocalDate(2026, 7, 5)..LocalDate(2026, 7, 11),
                        solarBirthdayList = listOf(solarBirthday),
                        lunarDateList = lunarDateList(start = LocalDate(2026, 7, 5), endInclusive = LocalDate(2026, 7, 11), lunarYear = 2026, month = 5, firstDay = 21),
                        expected = listOf(solarBirthday),
                    ),
                    Case(
                        dateRange = LocalDate(2026, 8, 17)..LocalDate(2026, 8, 23),
                        solarBirthdayList = emptyList(),
                        lunarDateList = lunarDateList(start = LocalDate(2026, 8, 17), endInclusive = LocalDate(2026, 8, 23), lunarYear = 2026, month = 7, firstDay = 5),
                        expected = listOf(lunarBirthday.toCalendar(date = LocalDate(2026, 8, 20))),
                    ),
                    Case(
                        dateRange = LocalDate(2027, 8, 8)..LocalDate(2027, 8, 14),
                        solarBirthdayList = emptyList(),
                        lunarDateList = lunarDateList(start = LocalDate(2027, 8, 8), endInclusive = LocalDate(2027, 8, 14), lunarYear = 2027, month = 7, firstDay = 7),
                        expected = listOf(lunarBirthday.toCalendar(date = LocalDate(2027, 8, 9))),
                    ),
                )

            When("각 기간을 표시 대상 기간으로 캘린더 생일을 조회한다") {
                Then("양력 생일은 저장된 월·일을, 음력 생일은 음력 자료로 찾은 양력 날짜를 차지한다") {
                    caseList.forEach { case ->
                        val useCase =
                            useCase(
                                account = account,
                                dateRange = case.dateRange,
                                solarBirthdayList = case.solarBirthdayList,
                                lunarBirthdayList = listOf(lunarBirthday),
                                lunarDateList = case.lunarDateList,
                            )

                        useCase(parameter = case.dateRange).first().shouldBeSuccess() shouldBe case.expected
                    }
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-017: 생일이 음력 2026년 12월 1일인 연락처가 있고 2026년·2027년 음력 자료가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val lunarBirthday = lunarContactBirthday(birthday = LocalDate(2026, 12, 1))
            val previousLunarYearRange = LocalDate(2026, 1, 18)..LocalDate(2026, 1, 24)
            val previousLunarYearDateList =
                lunarDateList(start = LocalDate(2026, 1, 18), endInclusive = LocalDate(2026, 1, 18), lunarYear = 2025, month = 11, firstDay = 30) +
                    lunarDateList(start = LocalDate(2026, 1, 19), endInclusive = LocalDate(2026, 1, 24), lunarYear = 2025, month = 12, firstDay = 1)
            val sameLunarYearRange = LocalDate(2027, 1, 3)..LocalDate(2027, 1, 9)
            val sameLunarYearDateList =
                lunarDateList(start = LocalDate(2027, 1, 3), endInclusive = LocalDate(2027, 1, 7), lunarYear = 2026, month = 11, firstDay = 26) +
                    lunarDateList(start = LocalDate(2027, 1, 8), endInclusive = LocalDate(2027, 1, 9), lunarYear = 2026, month = 12, firstDay = 1)

            When("음력 연도가 저장된 연도보다 이전인 기간을 조회한다") {
                val useCase = useCase(account = account, dateRange = previousLunarYearRange, lunarBirthdayList = listOf(lunarBirthday), lunarDateList = previousLunarYearDateList)

                Then("결과에 포함되지 않는다") {
                    useCase(parameter = previousLunarYearRange).first().shouldBeSuccess() shouldBe emptyList()
                }
            }

            When("음력 연도가 저장된 연도와 같은 기간을 조회한다") {
                val useCase = useCase(account = account, dateRange = sameLunarYearRange, lunarBirthdayList = listOf(lunarBirthday), lunarDateList = sameLunarYearDateList)

                Then("그 양력 날짜를 차지하는 생일이 결과에 포함된다") {
                    useCase(parameter = sameLunarYearRange).first().shouldBeSuccess() shouldBe listOf(lunarBirthday.toCalendar(date = LocalDate(2027, 1, 8)))
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-018: 생일이 음력 1990년 6월 1일인 연락처가 있고 윤6월이 있는 2025년 음력 자료가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDate(2025, 6, 1)..LocalDate(2025, 8, 31)
            val lunarBirthday = lunarContactBirthday(birthday = LocalDate(1990, 6, 1))
            val lunarDateList =
                lunarDateList(start = LocalDate(2025, 6, 1), endInclusive = LocalDate(2025, 6, 24), lunarYear = 2025, month = 5, firstDay = 6) +
                    lunarDateList(start = LocalDate(2025, 6, 25), endInclusive = LocalDate(2025, 7, 24), lunarYear = 2025, month = 6, firstDay = 1) +
                    lunarDateList(start = LocalDate(2025, 7, 25), endInclusive = LocalDate(2025, 8, 22), lunarYear = 2025, month = 6, firstDay = 1, isLeapMonth = true) +
                    lunarDateList(start = LocalDate(2025, 8, 23), endInclusive = LocalDate(2025, 8, 31), lunarYear = 2025, month = 7, firstDay = 1)
            val useCase = useCase(account = account, dateRange = dateRange, lunarBirthdayList = listOf(lunarBirthday), lunarDateList = lunarDateList)

            When("2025년 6월 1일부터 8월 31일까지를 표시 대상 기간으로 조회한다") {
                Then("평달 6월 1일인 6월 25일만 차지하고 윤달 6월 1일인 7월 25일은 차지하지 않는다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe listOf(lunarBirthday.toCalendar(date = LocalDate(2025, 6, 25)))
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-019: 생일이 음력 1990년 7월 30일인 연락처가 있고 2025년·2026년 음력 자료가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val lunarBirthday = lunarContactBirthday(birthday = LocalDate(1990, 7, 30))
            val longMonthRange = LocalDate(2025, 9, 19)..LocalDate(2025, 9, 25)
            val longMonthDateList =
                lunarDateList(start = LocalDate(2025, 9, 19), endInclusive = LocalDate(2025, 9, 21), lunarYear = 2025, month = 7, firstDay = 28) +
                    lunarDateList(start = LocalDate(2025, 9, 22), endInclusive = LocalDate(2025, 9, 25), lunarYear = 2025, month = 8, firstDay = 1)
            val shortMonthRange = LocalDate(2026, 9, 8)..LocalDate(2026, 9, 14)
            val shortMonthDateList =
                lunarDateList(start = LocalDate(2026, 9, 8), endInclusive = LocalDate(2026, 9, 10), lunarYear = 2026, month = 7, firstDay = 27) +
                    lunarDateList(start = LocalDate(2026, 9, 11), endInclusive = LocalDate(2026, 9, 14), lunarYear = 2026, month = 8, firstDay = 1)

            When("음력 7월 30일이 있는 2025년의 기간을 조회한다") {
                val useCase = useCase(account = account, dateRange = longMonthRange, lunarBirthdayList = listOf(lunarBirthday), lunarDateList = longMonthDateList)

                Then("2025년 9월 21일을 차지하는 생일이 결과에 포함된다") {
                    useCase(parameter = longMonthRange).first().shouldBeSuccess() shouldBe listOf(lunarBirthday.toCalendar(date = LocalDate(2025, 9, 21)))
                }
            }

            When("음력 7월이 29일까지인 2026년의 기간을 조회한다") {
                val useCase = useCase(account = account, dateRange = shortMonthRange, lunarBirthdayList = listOf(lunarBirthday), lunarDateList = shortMonthDateList)

                Then("결과에 포함되지 않고 다른 날짜로 옮겨지지도 않는다") {
                    useCase(parameter = shortMonthRange).first().shouldBeSuccess() shouldBe emptyList()
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-020: 음력 1990년 7월 8일 생일 연락처와 양력 1990년 8월 20일 생일 연락처가 있고 2026년 음력 자료가 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDate(2026, 8, 17)..LocalDate(2026, 8, 23)
            val solarBirthday = calendarContactBirthday(name = "양력", date = LocalDate(2026, 8, 20))
            val lunarBirthday = lunarContactBirthday(name = "음력", birthday = LocalDate(1990, 7, 8))
            val useCase =
                useCase(
                    account = account,
                    dateRange = dateRange,
                    solarBirthdayList = listOf(solarBirthday),
                    lunarBirthdayList = listOf(lunarBirthday),
                    lunarDateList = emptyList(),
                )

            When("2026년 8월 17일부터 8월 23일까지를 표시 대상 기간으로 조회한다") {
                Then("양력 생일만 결과에 포함되고 음력 생일은 포함되지 않는다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe listOf(solarBirthday)
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-023: 음력 1990년 7월 8일 생일 연락처와 양력 1990년 8월 20일 생일 연락처가 있고 기기에 저장된 음력 자료를 읽지 못한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDate(2026, 8, 17)..LocalDate(2026, 8, 23)
            val solarBirthday = calendarContactBirthday(date = LocalDate(2026, 8, 20))
            val lunarBirthday = lunarContactBirthday(birthday = LocalDate(1990, 7, 8))
            val lunarRepository = mockk<LunarRepository>()
            every { lunarRepository.get(dateRange = dateRange) } returns flow { throw IllegalStateException(fixtureMonkey.giveMeOne<String>()) }
            val useCase =
                useCase(
                    account = account,
                    dateRange = dateRange,
                    solarBirthdayList = listOf(solarBirthday),
                    lunarBirthdayList = listOf(lunarBirthday),
                    lunarRepository = lunarRepository,
                )

            When("2026년 8월 17일부터 8월 23일까지를 표시 대상 기간으로 조회한다") {
                Then("조회가 성공하고 양력 생일만 결과에 포함된다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe listOf(solarBirthday)
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DOMAIN-021: 양력 8월 21일 `가`, 음력 7월 8일 `나`, 양력 8월 20일 `다` 연락처가 있고 2026년 음력 자료가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDate(2026, 8, 17)..LocalDate(2026, 8, 23)
            val first = calendarContactBirthday(name = "가", date = LocalDate(2026, 8, 21))
            val second = lunarContactBirthday(name = "나", birthday = LocalDate(1990, 7, 8))
            val third = calendarContactBirthday(name = "다", date = LocalDate(2026, 8, 20))
            val useCase =
                useCase(
                    account = account,
                    dateRange = dateRange,
                    solarBirthdayList = listOf(third, first),
                    lunarBirthdayList = listOf(second),
                    lunarDateList = lunarDateList(start = LocalDate(2026, 8, 17), endInclusive = LocalDate(2026, 8, 23), lunarYear = 2026, month = 7, firstDay = 5),
                )

            When("2026년 8월 17일부터 8월 23일까지를 표시 대상 기간으로 조회한다") {
                Then("8월 20일의 `나`, 8월 20일의 `다`, 8월 21일의 `가` 순으로 하나의 결과에 담긴다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess() shouldBe listOf(second.toCalendar(date = LocalDate(2026, 8, 20)), third, first)
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DATA-006: 음력 1990년 7월 8일 생일 연락처가 있고 2026년 음력 자료가 없는 상태로 결과를 계속 관찰하고 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = LocalDate(2026, 8, 17)..LocalDate(2026, 8, 23)
            val lunarBirthday = lunarContactBirthday(birthday = LocalDate(1990, 7, 8))
            val lunarDateFlow = MutableStateFlow<List<LunarDate>>(emptyList())
            val lunarRepository = mockk<LunarRepository>()
            every { lunarRepository.get(dateRange = dateRange) } returns lunarDateFlow
            val useCase =
                useCase(
                    account = account,
                    dateRange = dateRange,
                    lunarBirthdayList = listOf(lunarBirthday),
                    lunarRepository = lunarRepository,
                )

            When("2026년의 음력 자료가 로컬 캐시에 채워진다") {
                Then("빈 결과에 이어 2026년 8월 20일을 차지하는 생일이 담긴 결과가 전달된다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe emptyList()

                        lunarDateFlow.value = lunarDateList(start = LocalDate(2026, 8, 17), endInclusive = LocalDate(2026, 8, 23), lunarYear = 2026, month = 7, firstDay = 5)

                        awaitItem().shouldBeSuccess() shouldBe listOf(lunarBirthday.toCalendar(date = LocalDate(2026, 8, 20)))
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        Given("TC-CALENDAR-CONTACT-BIRTHDAY-DATA-007: 음력 생일 연락처가 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val dateRange = dateRange()
            val lunarRepository = mockk<LunarRepository>()
            every { lunarRepository.get(dateRange = dateRange) } returns flowOf(emptyList())
            val useCase =
                useCase(
                    account = account,
                    dateRange = dateRange,
                    lunarBirthdayList = listOf(lunarContactBirthday(birthday = LocalDate(1990, 7, 8))),
                    lunarRepository = lunarRepository,
                )

            When("표시 대상 기간으로 캘린더 생일을 조회한다") {
                Then("음력 자료는 로컬 캐시에서만 읽고 원격 동기화는 요청되지 않는다") {
                    useCase(parameter = dateRange).first().shouldBeSuccess()

                    verify(exactly = 1) { lunarRepository.get(dateRange = dateRange) }
                    coVerify(exactly = 0) { lunarRepository.fetch(year = any()) }
                }
            }
        }
    }) {
    public companion object {
        private fun dateRange(): LocalDateRange = LocalDateRange(LocalDate(2026, 6, 28), LocalDate(2026, 8, 8))

        private fun repository(): AccountCalendarContactBirthdayRepository =
            mockk<AccountCalendarContactBirthdayRepository>().also { repository ->
                every { repository.getLunar(account = any()) } returns flowOf(emptyList())
            }

        private fun useCase(
            getAccountUseCase: GetAccountUseCase,
            repository: AccountCalendarContactBirthdayRepository,
            lunarRepository: LunarRepository = mockk<LunarRepository>().also { every { it.get(dateRange = any()) } returns flowOf(emptyList()) },
        ): GetCalendarContactBirthdayUseCase =
            GetCalendarContactBirthdayUseCase(
                getAccountUseCase = getAccountUseCase,
                accountCalendarContactBirthdayRepository = repository,
                lunarRepository = lunarRepository,
            )

        private fun useCase(
            account: Account,
            dateRange: LocalDateRange,
            solarBirthdayList: List<CalendarContactBirthday> = emptyList(),
            lunarBirthdayList: List<LunarContactBirthday>,
            lunarDateList: List<LunarDate> = emptyList(),
            lunarRepository: LunarRepository = mockk<LunarRepository>().also { every { it.get(dateRange = dateRange) } returns flowOf(lunarDateList) },
        ): GetCalendarContactBirthdayUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val repository = mockk<AccountCalendarContactBirthdayRepository>()
            every { repository.get(account = account, dateRange = dateRange) } returns flowOf(solarBirthdayList)
            every { repository.getLunar(account = account) } returns flowOf(lunarBirthdayList)

            return useCase(getAccountUseCase = getAccountUseCase, repository = repository, lunarRepository = lunarRepository)
        }

        private fun calendarContactBirthday(
            name: String = fixtureMonkey.giveMeOne(),
            date: LocalDate = LocalDate(2026, 7, 8),
        ): CalendarContactBirthday =
            fixtureMonkey
                .giveMeKotlinBuilder<CalendarContactBirthday>()
                .setExp(CalendarContactBirthday::name, name)
                .setExp(CalendarContactBirthday::date, date)
                .sample()

        private fun lunarContactBirthday(
            name: String = fixtureMonkey.giveMeOne(),
            birthday: LocalDate,
        ): LunarContactBirthday =
            LunarContactBirthday(
                contactId = Uuid.random(),
                name = name,
                birthday = birthday,
            )

        private fun LunarContactBirthday.toCalendar(date: LocalDate): CalendarContactBirthday =
            CalendarContactBirthday(
                contactId = contactId,
                name = name,
                date = date,
            )

        private fun lunarDateList(
            start: LocalDate,
            endInclusive: LocalDate,
            lunarYear: Int,
            month: Int,
            firstDay: Int,
            isLeapMonth: Boolean = false,
        ): List<LunarDate> =
            (start..endInclusive).mapIndexed { index, solar ->
                LunarDate(
                    solar = solar,
                    year = lunarYear,
                    month = month,
                    day = firstDay + index,
                    isLeapMonth = isLeapMonth,
                )
            }

        private data class Case(
            val dateRange: LocalDateRange,
            val solarBirthdayList: List<CalendarContactBirthday>,
            val lunarDateList: List<LunarDate>,
            val expected: List<CalendarContactBirthday>,
        )
    }
}
