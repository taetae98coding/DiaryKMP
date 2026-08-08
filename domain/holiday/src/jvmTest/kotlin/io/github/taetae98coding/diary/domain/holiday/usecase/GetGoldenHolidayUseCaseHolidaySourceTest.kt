package io.github.taetae98coding.diary.domain.holiday.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

// 2025년 12월 31일은 수요일, 2026년 1월 1일은 목요일, 2026년 12월 31일은 목요일이다.
class GetGoldenHolidayUseCaseHolidaySourceTest :
    BehaviorSpec({
        Given("TC-HOLIDAY-HOME-DATA-005 앞뒤 년도에 걸친 공휴일이 각 년도에 나뉘어 저장되어 있다") {
            val repository = mockk<HolidayRepository>()
            every { repository.get(year = 2025) } returns
                flowOf(listOf(holiday(start = date(year = 2025, month = Month.DECEMBER, day = 31))))
            every { repository.get(year = 2026) } returns
                flowOf(
                    listOf(
                        holiday(
                            start = date(year = 2026, month = Month.JANUARY, day = 1),
                            endInclusive = date(year = 2026, month = Month.JANUARY, day = 2),
                        ),
                        holiday(start = date(year = 2026, month = Month.DECEMBER, day = 31)),
                    ),
                )
            every { repository.get(year = 2027) } returns
                flowOf(listOf(holiday(start = date(year = 2027, month = Month.JANUARY, day = 1))))

            When("2026년의 황금연휴를 조회한다") {
                Then("앞뒤 년도 공휴일까지 이어 붙인 황금연휴가 나오고 그 밖의 년도는 조회하지 않는다") {
                    goldenHolidayUseCase(repository)(parameter = parameter(year = 2026)).test {
                        val optionList = awaitItem().shouldBeSuccess().flatMap { group -> group.optionList }

                        optionList.first().dateRange.start shouldBe date(year = 2025, month = Month.DECEMBER, day = 31)
                        optionList.last().dateRange.endInclusive shouldBe date(year = 2027, month = Month.JANUARY, day = 3)
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.get(year = 2024) }
                    verify(exactly = 0) { repository.get(year = 2028) }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DATA-006 조회 중인 년도의 공휴일이 바뀐다") {
            val holidayFlow = MutableStateFlow(listOf(holiday(start = february(day = 6))))
            val repository = mockk<HolidayRepository>()
            every { repository.get(year = any()) } answers {
                if (firstArg<Int>() == YEAR) holidayFlow else flowOf(emptyList())
            }

            When("2026년의 황금연휴를 계속 조회한다") {
                Then("바뀐 공휴일로 다시 계산한 황금연휴를 이어서 제공한다") {
                    goldenHolidayUseCase(repository)(parameter = parameter(year = YEAR)).test {
                        awaitItem()
                            .shouldBeSuccess()
                            .singleOption()
                            .dateRange.start shouldBe february(day = 6)

                        holidayFlow.value = listOf(holiday(start = february(day = 13)))

                        awaitItem()
                            .shouldBeSuccess()
                            .singleOption()
                            .dateRange.start shouldBe february(day = 13)
                    }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DATA-007 모든 대상 년도의 공휴일 조회 결과가 비어 있거나 실패한다") {
            val emptyRepository = mockk<HolidayRepository>()
            every { emptyRepository.get(year = any()) } returns flowOf(emptyList())
            val failedRepository = mockk<HolidayRepository>()
            every { failedRepository.get(year = any()) } returns flow { throw IllegalStateException(fixtureMonkey.giveMeOne<String>()) }

            When("2026년의 황금연휴를 조회한다") {
                Then("오류 없이 빈 황금연휴 목록이 나온다") {
                    listOf(emptyRepository, failedRepository).forEach { repository ->
                        goldenHolidayUseCase(repository)(parameter = parameter(year = YEAR)).test {
                            awaitItem().shouldBeSuccess() shouldBe emptyList()
                            awaitComplete()
                        }
                    }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DATA-008 한 대상 년도의 공휴일 조회만 실패한다") {
            val holiday = holiday(start = february(day = 6))
            val repository = mockk<HolidayRepository>()
            every { repository.get(year = any()) } answers {
                if (firstArg<Int>() == YEAR) flowOf(listOf(holiday)) else flowOf(emptyList())
            }
            every { repository.get(year = YEAR - 1) } returns flow { throw IllegalStateException(fixtureMonkey.giveMeOne<String>()) }

            When("2026년의 황금연휴를 조회한다") {
                Then("실패하지 않은 년도의 공휴일로 만든 황금연휴가 나온다") {
                    goldenHolidayUseCase(repository)(parameter = parameter(year = YEAR)).test {
                        awaitItem().shouldBeSuccess().singleOption().dateRange shouldBe february(day = 6)..february(day = 8)
                        awaitComplete()
                    }
                }
            }
        }
    })

private const val YEAR = 2026

private fun List<GoldenHolidayGroup>.singleOption(): GoldenHoliday = single().optionList.single()

private fun goldenHolidayUseCase(holidayRepository: HolidayRepository): GetGoldenHolidayUseCase =
    GetGoldenHolidayUseCase(
        getHolidayUseCase = GetHolidayUseCase(holidayRepository = holidayRepository),
    )

private fun parameter(year: Int): GetGoldenHolidayUseCase.Parameter = GetGoldenHolidayUseCase.Parameter(year = year, annualLeaveCount = 0)

private fun holiday(
    start: LocalDate,
    endInclusive: LocalDate = start,
): Holiday =
    Holiday(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = true,
        dateRange = LocalDateRange(start = start, endInclusive = endInclusive),
    )

private fun date(
    year: Int,
    month: Month,
    day: Int,
): LocalDate = LocalDate(year = year, month = month, day = day)

private fun february(day: Int): LocalDate = date(year = YEAR, month = Month.FEBRUARY, day = day)
