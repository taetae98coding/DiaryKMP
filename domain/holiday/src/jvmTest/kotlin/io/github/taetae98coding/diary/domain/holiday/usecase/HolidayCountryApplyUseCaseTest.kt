package io.github.taetae98coding.diary.domain.holiday.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private const val HOLIDAY_YEAR = 2026

private val BOTH_OPTION_SET: Set<HolidayCountryOption> = setOf(HolidayCountryOption.KOREA, HolidayCountryOption.UNITED_STATES)

private val countryOptionMap: Map<HolidayCountry, HolidayCountryOption> =
    mapOf(
        HolidayCountry.KOREA to HolidayCountryOption.KOREA,
        HolidayCountry.UNITED_STATES to HolidayCountryOption.UNITED_STATES,
    )

class HolidayCountryApplyUseCaseTest :
    BehaviorSpec({
        Given("TC-HOLIDAY-FETCH-DOMAIN-006 적용 국가가 한국과 미국이고 두 국가의 원격 조회가 성공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val koreaHolidayList = listOf(holiday(name = "korea"))
            val unitedStatesHolidayList = listOf(holiday(name = "united-states"))
            val repository = mockk<HolidayRepository>()
            coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } returns koreaHolidayList
            coEvery { repository.fetch(country = HolidayCountry.UNITED_STATES, year = year) } returns unitedStatesHolidayList
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = countrySettingUseCase(optionSet = BOTH_OPTION_SET), holidayRepository = repository)

            When("해당 연도의 공휴일 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("동기화가 성공하고 결과에 두 국가의 공휴일이 모두 담긴다") {
                    result.shouldBeSuccess() shouldContainExactlyInAnyOrder koreaHolidayList + unitedStatesHolidayList
                }
            }
        }

        Given("TC-HOLIDAY-FETCH-DOMAIN-007 적용 국가가 없다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val repository = mockk<HolidayRepository>()
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = countrySettingUseCase(optionSet = emptySet()), holidayRepository = repository)

            When("특정 연도의 공휴일 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("원격 조회가 수행되지 않고 빈 공휴일 목록으로 성공한다") {
                    result.shouldBeSuccess() shouldBe emptyList()
                    coVerify(exactly = 0) { repository.fetch(country = any(), year = any()) }
                }
            }
        }

        Given("TC-HOLIDAY-FETCH-DOMAIN-008 한국 원격 조회는 실패하고 미국 원격 조회는 성공한다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<HolidayRepository>()
            coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } throws failure
            coEvery { repository.fetch(country = HolidayCountry.UNITED_STATES, year = year) } returns listOf(holiday(name = "united-states"))
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = countrySettingUseCase(optionSet = BOTH_OPTION_SET), holidayRepository = repository)

            When("해당 연도의 공휴일 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("동기화는 실패하고 미국의 해당 연도 동기화는 수행된다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                    coVerify(exactly = 1) { repository.fetch(country = HolidayCountry.UNITED_STATES, year = year) }
                }
            }
        }

        Given("TC-HOLIDAY-FETCH-DOMAIN-009 저장된 국가 설정을 읽을 수 없다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val repository = mockk<HolidayRepository>()
            val useCase =
                FetchHolidayUseCase(
                    getHolidayCountrySettingUseCase = countrySettingUseCase(optionSetFlow = flow { throw IllegalStateException(fixtureMonkey.giveMeOne<String>()) }),
                    holidayRepository = repository,
                )

            When("특정 연도의 공휴일 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("원격 조회가 수행되지 않고 동기화가 실패한다") {
                    result.shouldBeFailure()
                    coVerify(exactly = 0) { repository.fetch(country = any(), year = any()) }
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DATA-014 같은 연도의 한국과 미국 공휴일이 저장되어 있다") {
            val year = HOLIDAY_YEAR
            val holidayMap = countryHolidayMap()
            val caseList =
                listOf(
                    setOf(HolidayCountry.KOREA),
                    setOf(HolidayCountry.KOREA, HolidayCountry.UNITED_STATES),
                    emptySet(),
                )

            When("적용 국가별로 그 연도의 캘린더용 공휴일을 조회한다") {
                Then("적용 국가의 공휴일만 제공된다") {
                    caseList.forEach { countrySet ->
                        calendarHolidayUseCase(
                            optionSet = countrySet.mapTo(mutableSetOf()) { country -> countryOptionMap.getValue(country) },
                            holidayRepository = holidayRepository(holidayMap = holidayMap),
                        )(parameter = year)
                            .first()
                            .shouldBeSuccess() shouldContainExactlyInAnyOrder countrySet.flatMap { country -> holidayMap.getValue(country) }
                    }
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DATA-015 적용 국가가 한국인 상태로 캘린더용 공휴일을 조회하고 있다") {
            val year = HOLIDAY_YEAR
            val holidayMap = countryHolidayMap()
            val optionSetFlow = MutableStateFlow(setOf(HolidayCountryOption.KOREA))
            val useCase =
                GetCalendarHolidayUseCase(
                    getHolidayCountrySettingUseCase = countrySettingUseCase(optionSetFlow = optionSetFlow),
                    holidayRepository = holidayRepository(holidayMap = holidayMap),
                    holidaySettingRepository = hiddenKeySettingRepository(hiddenKeySet = emptySet()),
                )

            When("국가 설정에 미국을 더한다") {
                Then("한국과 미국 공휴일이 이어서 제공된다") {
                    useCase(parameter = year).test {
                        awaitItem().shouldBeSuccess() shouldBe holidayMap.getValue(HolidayCountry.KOREA)

                        optionSetFlow.value = BOTH_OPTION_SET

                        awaitItem().shouldBeSuccess() shouldContainExactlyInAnyOrder holidayMap.values.flatten()
                    }
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DATA-016 공휴일이 저장되어 있고 저장된 국가 설정을 읽을 수 없다") {
            val year = HOLIDAY_YEAR
            val useCase =
                GetCalendarHolidayUseCase(
                    getHolidayCountrySettingUseCase = countrySettingUseCase(optionSetFlow = flow { throw IllegalStateException(fixtureMonkey.giveMeOne<String>()) }),
                    holidayRepository = holidayRepository(holidayMap = countryHolidayMap()),
                    holidaySettingRepository = hiddenKeySettingRepository(hiddenKeySet = emptySet()),
                )

            When("그 연도의 캘린더용 공휴일을 조회한다") {
                Then("조회 결과가 실패로 제공된다") {
                    useCase(parameter = year).first().shouldBeFailure()
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-012 한국과 미국 공휴일이 저장되어 있다") {
            val holidayMap = countryHolidayMap()
            val caseList =
                listOf(
                    setOf(HolidayCountry.KOREA),
                    setOf(HolidayCountry.UNITED_STATES),
                    setOf(HolidayCountry.KOREA, HolidayCountry.UNITED_STATES),
                    emptySet(),
                )

            When("적용 국가별로 공휴일 항목을 구성한다") {
                Then("적용 국가의 공휴일만 항목이 된다") {
                    caseList.forEach { countrySet ->
                        val setting =
                            GetSettingHolidayUseCase(
                                getHolidayCountrySettingUseCase =
                                    countrySettingUseCase(optionSet = countrySet.mapTo(mutableSetOf()) { country -> countryOptionMap.getValue(country) }),
                                holidayRepository = holidayRepository(holidayMap = holidayMap),
                                holidaySettingRepository = hiddenKeySettingRepository(hiddenKeySet = emptySet()),
                            )(parameter = Unit).first().shouldBeSuccess()

                        setting.map { holidaySetting -> holidaySetting.name } shouldContainExactlyInAnyOrder
                            countrySet.flatMap { country -> holidayMap.getValue(country) }.map { holiday -> holiday.name }
                    }
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-DOMAIN-013 적용 국가가 한국이고 미국 공휴일 key가 숨김으로 저장되어 있다") {
            val koreaHoliday = holiday(name = "korea-day", isHoliday = false)
            val unitedStatesHoliday = holiday(name = "united-states-day", isHoliday = false)
            val holidayMap =
                mapOf(
                    HolidayCountry.KOREA to listOf(koreaHoliday),
                    HolidayCountry.UNITED_STATES to listOf(unitedStatesHoliday),
                )

            When("전체 해제를 실행한다") {
                val repository = hiddenKeySettingRepository(hiddenKeySet = setOf(unitedStatesHoliday.name))
                val submitted = slot<Set<String>>()
                coEvery { repository.submitHiddenKeySet(hiddenKeySet = capture(submitted)) } returns Unit

                DeselectAllHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository(holidayMap = holidayMap),
                    holidaySettingRepository = repository,
                )(parameter = Unit).shouldBeSuccess()

                Then("숨김 key 집합이 한국 공휴일 key로 교체된다") {
                    submitted.captured shouldBe setOf(koreaHoliday.name)
                }
            }

            When("쉬는 날만 선택을 실행한다") {
                val repository = hiddenKeySettingRepository(hiddenKeySet = setOf(unitedStatesHoliday.name))
                val submitted = slot<Set<String>>()
                coEvery { repository.submitHiddenKeySet(hiddenKeySet = capture(submitted)) } returns Unit

                SelectDaysOffHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository(holidayMap = holidayMap),
                    holidaySettingRepository = repository,
                )(parameter = Unit).shouldBeSuccess()

                Then("숨김 key 집합이 한국 공휴일 key로 교체된다") {
                    submitted.captured shouldBe setOf(koreaHoliday.name)
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DATA-013 2026년 5월 1일은 미국 공휴일로만, 5월 4일은 한국 공휴일로만 저장되어 있고 연차 개수는 0이다") {
            val holidayMap =
                mapOf(
                    HolidayCountry.KOREA to listOf(holiday(name = "korea-day", isHoliday = true, date = may(day = 4))),
                    HolidayCountry.UNITED_STATES to listOf(holiday(name = "united-states-day", isHoliday = true, date = may(day = 1))),
                )
            val caseList =
                listOf(
                    setOf(HolidayCountryOption.KOREA) to LocalDateRange(start = may(day = 2), endInclusive = may(day = 4)),
                    setOf(HolidayCountryOption.UNITED_STATES) to LocalDateRange(start = may(day = 1), endInclusive = may(day = 3)),
                    BOTH_OPTION_SET to LocalDateRange(start = may(day = 1), endInclusive = may(day = 4)),
                )

            When("적용 국가별로 2026년의 황금연휴를 조회한다") {
                Then("적용 국가의 공휴일을 합쳐 판단한 황금연휴가 나온다") {
                    caseList.forEach { (optionSet, dateRange) ->
                        GetGoldenHolidayUseCase(
                            getHolidayUseCase =
                                GetHolidayUseCase(
                                    getHolidayCountrySettingUseCase = countrySettingUseCase(optionSet = optionSet),
                                    holidayRepository = holidayRepository(holidayMap = holidayMap),
                                ),
                        )(parameter = GetGoldenHolidayUseCase.Parameter(year = 2026, annualLeaveCount = 0))
                            .first()
                            .shouldBeSuccess()
                            .map { group -> group.optionList.single().dateRange } shouldBe listOf(dateRange)
                    }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DATA-015 5월 1일은 미국 공휴일로만, 5월 4일은 한국 공휴일로만 저장되어 있고 적용 국가가 한국인 채 2026년의 황금연휴를 조회하고 있다") {
            val holidayMap =
                mapOf(
                    HolidayCountry.KOREA to listOf(holiday(name = "korea-day", isHoliday = true, date = may(day = 4))),
                    HolidayCountry.UNITED_STATES to listOf(holiday(name = "united-states-day", isHoliday = true, date = may(day = 1))),
                )
            val optionSetFlow = MutableStateFlow(setOf(HolidayCountryOption.KOREA))
            val useCase =
                GetGoldenHolidayUseCase(
                    getHolidayUseCase =
                        GetHolidayUseCase(
                            getHolidayCountrySettingUseCase = countrySettingUseCase(optionSetFlow = optionSetFlow),
                            holidayRepository = holidayRepository(holidayMap = holidayMap),
                        ),
                )

            When("국가 설정이 바뀌어 적용 국가가 한국과 미국이 된다") {
                Then("바뀐 적용 국가의 공휴일로 다시 계산한 황금연휴를 이어서 제공한다") {
                    useCase(parameter = GetGoldenHolidayUseCase.Parameter(year = 2026, annualLeaveCount = 0)).test {
                        awaitItem()
                            .shouldBeSuccess()
                            .map { group -> group.optionList.single().dateRange } shouldBe listOf(may(day = 2)..may(day = 4))

                        optionSetFlow.value = BOTH_OPTION_SET

                        // 대상 년도마다 국가 설정을 따로 받아 합치므로 바뀐 결과 앞뒤에 같은 결과가 더 올 수 있다.
                        val expected = listOf(may(day = 1)..may(day = 4))
                        var actual: List<LocalDateRange>
                        do {
                            actual = awaitItem().shouldBeSuccess().map { group -> group.optionList.single().dateRange }
                        } while (actual != expected)
                        actual shouldBe expected
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    })

private fun calendarHolidayUseCase(
    optionSet: Set<HolidayCountryOption>,
    holidayRepository: HolidayRepository,
): GetCalendarHolidayUseCase =
    GetCalendarHolidayUseCase(
        getHolidayCountrySettingUseCase = countrySettingUseCase(optionSet = optionSet),
        holidayRepository = holidayRepository,
        holidaySettingRepository = hiddenKeySettingRepository(hiddenKeySet = emptySet()),
    )

private fun hiddenKeySettingRepository(hiddenKeySet: Set<String>): HolidaySettingRepository =
    mockk<HolidaySettingRepository>().also { repository ->
        every { repository.getHiddenKeySet() } returns flowOf(hiddenKeySet)
    }

// 연도 조회는 요청 연도에 시작하는 공휴일만, 전체 조회는 모든 공휴일을 적용 국가로 걸러 제공한다.
private fun holidayRepository(holidayMap: Map<HolidayCountry, List<Holiday>>): HolidayRepository =
    mockk<HolidayRepository>().also { repository ->
        every { repository.get(countrySet = any()) } answers {
            flowOf(firstArg<Set<HolidayCountry>>().flatMap { country -> holidayMap[country].orEmpty() })
        }
        every { repository.get(countrySet = any(), year = any()) } answers {
            val year = secondArg<Int>()
            flowOf(
                firstArg<Set<HolidayCountry>>()
                    .flatMap { country -> holidayMap[country].orEmpty() }
                    .filter { holiday -> holiday.dateRange.start.year == year },
            )
        }
    }

private fun countryHolidayMap(): Map<HolidayCountry, List<Holiday>> =
    mapOf(
        HolidayCountry.KOREA to listOf(holiday(name = "korea")),
        HolidayCountry.UNITED_STATES to listOf(holiday(name = "united-states")),
    )

private fun holiday(
    name: String,
    isHoliday: Boolean = fixtureMonkey.giveMeOne(),
    date: LocalDate? = null,
): Holiday {
    val start = date ?: LocalDate(year = HOLIDAY_YEAR, month = 1, day = 1)

    return Holiday(
        name = if (date == null) "$name-${fixtureMonkey.giveMeOne<String>()}" else name,
        isHoliday = isHoliday,
        dateRange = start..start,
    )
}

private fun may(day: Int): LocalDate = LocalDate(year = 2026, month = 5, day = day)
