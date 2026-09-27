package io.github.taetae98coding.diary.domain.holiday.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayVisibilityActionUseCaseTest :
    BehaviorSpec({
        Given("선택된 공휴일 key가 있다") {
            val target = holidaySetting(name = uniqueName(label = "target"), isVisible = true)
            val getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingList = listOf(target))
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.addHiddenKey(key = target.name) } just Runs
            val useCase =
                ToggleHolidayVisibilityUseCase(
                    getSettingHolidayUseCase = getSettingHolidayUseCase,
                    holidaySettingRepository = repository,
                )

            When("공휴일 선택 상태를 바꾼다") {
                Then("TC-SETTING-HOLIDAY-FEATURE-008 해당 key를 숨김 설정에 추가한다") {
                    useCase(parameter = target.name).shouldBeSuccess()

                    verify(exactly = 1) { getSettingHolidayUseCase(parameter = Unit) }
                    coVerify(exactly = 1) { repository.addHiddenKey(key = target.name) }
                    coVerify(exactly = 0) {
                        repository.removeHiddenKey(key = any())
                        repository.submitHiddenKeySet(hiddenKeySet = any())
                    }
                }
            }
        }

        Given("선택 해제된 공휴일 key가 있다") {
            val target = holidaySetting(name = uniqueName(label = "target"), isVisible = false)
            val getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingList = listOf(target))
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.removeHiddenKey(key = target.name) } just Runs
            val useCase =
                ToggleHolidayVisibilityUseCase(
                    getSettingHolidayUseCase = getSettingHolidayUseCase,
                    holidaySettingRepository = repository,
                )

            When("공휴일 선택 상태를 바꾼다") {
                Then("TC-SETTING-HOLIDAY-FEATURE-008 해당 key를 숨김 설정에서 제거한다") {
                    useCase(parameter = target.name).shouldBeSuccess()

                    verify(exactly = 1) { getSettingHolidayUseCase(parameter = Unit) }
                    coVerify(exactly = 1) { repository.removeHiddenKey(key = target.name) }
                    coVerify(exactly = 0) {
                        repository.addHiddenKey(key = any())
                        repository.submitHiddenKeySet(hiddenKeySet = any())
                    }
                }
            }
        }

        Given("현재 공휴일 목록에 없는 key가 있다") {
            val getSettingHolidayUseCase =
                getSettingHolidayUseCase(
                    holidaySettingList = listOf(holidaySetting(name = uniqueName(label = "known"))),
                )
            val repository = mockk<HolidaySettingRepository>(relaxed = true)
            val useCase =
                ToggleHolidayVisibilityUseCase(
                    getSettingHolidayUseCase = getSettingHolidayUseCase,
                    holidaySettingRepository = repository,
                )

            When("알 수 없는 key의 선택 상태를 바꾸려 한다") {
                Then("저장값을 변경하지 않는다") {
                    useCase(parameter = uniqueName(label = "unknown")).shouldBeSuccess()

                    verify(exactly = 1) { getSettingHolidayUseCase(parameter = Unit) }
                    coVerify(exactly = 0) {
                        repository.addHiddenKey(key = any())
                        repository.removeHiddenKey(key = any())
                        repository.submitHiddenKeySet(hiddenKeySet = any())
                    }
                }
            }
        }

        Given("화면 반영 전에 같은 공휴일을 다시 누른다") {
            val selected = holidaySetting(name = uniqueName(label = "double-toggle"), isVisible = true)
            val deselected = selected.copy(isVisible = false)
            val getSettingHolidayUseCase =
                mockk<GetSettingHolidayUseCase>().also { useCase ->
                    every { useCase(parameter = Unit) } returnsMany
                        listOf(
                            flowOf(Result.success(listOf(selected))),
                            flowOf(Result.success(listOf(deselected))),
                        )
                }
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.addHiddenKey(key = selected.name) } just Runs
            coEvery { repository.removeHiddenKey(key = selected.name) } just Runs
            val useCase =
                ToggleHolidayVisibilityUseCase(
                    getSettingHolidayUseCase = getSettingHolidayUseCase,
                    holidaySettingRepository = repository,
                )

            When("같은 key를 연속 두 번 토글한다") {
                Then("각 동작을 최신 저장값에 적용해 처음 상태로 돌아온다") {
                    useCase(parameter = selected.name).shouldBeSuccess()
                    useCase(parameter = selected.name).shouldBeSuccess()

                    verify(exactly = 2) { getSettingHolidayUseCase(parameter = Unit) }
                    coVerifyOrder {
                        repository.addHiddenKey(key = selected.name)
                        repository.removeHiddenKey(key = selected.name)
                    }
                }
            }
        }

        Given("숨김 설정이 있다") {
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.submitHiddenKeySet(hiddenKeySet = emptySet()) } just Runs
            val useCase =
                SelectAllHolidayUseCase(
                    holidaySettingRepository = repository,
                )

            When("전체 선택한다") {
                Then("TC-SETTING-HOLIDAY-FEATURE-009 빈 숨김 key 집합을 제출한다") {
                    useCase(parameter = Unit).shouldBeSuccess()

                    verify(exactly = 0) { repository.getHiddenKeySet() }
                    coVerify(exactly = 1) { repository.submitHiddenKeySet(hiddenKeySet = emptySet()) }
                }
            }
        }

        Given("전체 선택 해제할 공휴일 목록이 있다") {
            val substituteHolidayKey = "대체공휴일"
            val midsummerDayKey = "초복"
            val holidayList =
                listOf(
                    holiday(name = substituteHolidayKey),
                    holiday(name = substituteHolidayKey),
                    holiday(name = midsummerDayKey),
                )
            val keySet = setOf(substituteHolidayKey, midsummerDayKey)
            val holidayRepository = holidayRepository(holidayList = holidayList)
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.submitHiddenKeySet(hiddenKeySet = keySet) } just Runs
            val useCase =
                DeselectAllHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )

            When("전체 선택 해제한다") {
                Then("TC-SETTING-HOLIDAY-FEATURE-010 중복을 제거한 모든 key를 제출한다") {
                    useCase(parameter = Unit).shouldBeSuccess()

                    verify(exactly = 1) { holidayRepository.get(countrySet = KOREA_COUNTRY_SET) }
                    verify(exactly = 0) { repository.getHiddenKeySet() }
                    coVerify(exactly = 1) { repository.submitHiddenKeySet(hiddenKeySet = keySet) }
                }
            }
        }

        Given("같은 key에 쉬는 날과 쉬는 날이 아닌 항목이 함께 있는 공휴일 목록이 있다") {
            val substituteHolidayKey = "대체공휴일"
            val workingDayKey = "초복"
            val dayOff =
                holiday(
                    name = substituteHolidayKey,
                    isHoliday = true,
                )
            val sameKeyWorkingDay =
                holiday(
                    name = substituteHolidayKey,
                    isHoliday = false,
                )
            val workingDay =
                holiday(
                    name = workingDayKey,
                    isHoliday = false,
                )
            val holidayRepository =
                holidayRepository(
                    holidayList = listOf(dayOff, sameKeyWorkingDay, workingDay),
                )
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.submitHiddenKeySet(hiddenKeySet = setOf(workingDayKey)) } just Runs
            val useCase =
                SelectDaysOffHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )

            When("쉬는 날만 선택한다") {
                Then("TC-SETTING-HOLIDAY-FEATURE-011 하나라도 쉬는 날인 key는 숨기지 않고 나머지 key만 제출한다") {
                    useCase(parameter = Unit).shouldBeSuccess()

                    verify(exactly = 1) { holidayRepository.get(countrySet = KOREA_COUNTRY_SET) }
                    verify(exactly = 0) { repository.getHiddenKeySet() }
                    coVerify(exactly = 1) {
                        repository.submitHiddenKeySet(hiddenKeySet = setOf(workingDayKey))
                    }
                }
            }
        }

        Given("TC-SETTING-HOLIDAY-FEATURE-012 저장된 숨긴 공휴일 이름 목록이 일괄 선택 결과와 이미 같다") {
            val dayOff = holiday(name = uniqueName(label = "day-off"), isHoliday = true)
            val workingDay = holiday(name = uniqueName(label = "working-day"), isHoliday = false)
            val holidayRepository = holidayRepository(holidayList = listOf(dayOff, workingDay))
            val caseList =
                listOf(
                    "전체 선택" to emptySet(),
                    "전체 해제" to setOf(dayOff.name, workingDay.name),
                    "쉬는 날만 선택" to setOf(workingDay.name),
                )

            When("테스트 데이터의 일괄 선택을 실행한다") {
                Then("저장된 목록과 같은 목록을 그대로 제출해 새 저장값을 만들지 않는다") {
                    caseList.forEach { (action, storedHiddenKeySet) ->
                        val repository = mockk<HolidaySettingRepository>()
                        every { repository.getHiddenKeySet() } returns flowOf(storedHiddenKeySet)
                        coEvery { repository.submitHiddenKeySet(hiddenKeySet = any()) } just Runs
                        val useCase: suspend () -> Result<Unit> =
                            mapOf(
                                "전체 선택" to suspend { SelectAllHolidayUseCase(holidaySettingRepository = repository)(parameter = Unit) },
                                "전체 해제" to
                                    suspend {
                                        DeselectAllHolidayUseCase(
                                            getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                                            holidayRepository = holidayRepository,
                                            holidaySettingRepository = repository,
                                        )(parameter = Unit)
                                    },
                                "쉬는 날만 선택" to
                                    suspend {
                                        SelectDaysOffHolidayUseCase(
                                            getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                                            holidayRepository = holidayRepository,
                                            holidaySettingRepository = repository,
                                        )(parameter = Unit)
                                    },
                            ).getValue(action)

                        useCase().shouldBeSuccess()

                        coVerify(exactly = 1) { repository.submitHiddenKeySet(hiddenKeySet = storedHiddenKeySet) }
                    }
                }
            }
        }

        Given("세 일괄 선택을 적용할 공휴일 목록이 있다") {
            val dayOff = holiday(name = uniqueName(label = "day-off"), isHoliday = true)
            val workingDay = holiday(name = uniqueName(label = "working-day"), isHoliday = false)
            val holidayList = listOf(dayOff, workingDay)
            val targetKeySet = setOf(dayOff.name, workingDay.name)
            val holidayRepository = holidayRepository(holidayList = holidayList)
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.submitHiddenKeySet(hiddenKeySet = any()) } just Runs
            val selectAllUseCase =
                SelectAllHolidayUseCase(
                    holidaySettingRepository = repository,
                )
            val deselectAllUseCase =
                DeselectAllHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )
            val selectDaysOffHolidayUseCase =
                SelectDaysOffHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )

            When("각 일괄 선택을 실행한다") {
                Then("각 동작의 최종 숨김 key 집합을 제출한다") {
                    selectAllUseCase(parameter = Unit).shouldBeSuccess()
                    deselectAllUseCase(parameter = Unit).shouldBeSuccess()
                    selectDaysOffHolidayUseCase(parameter = Unit).shouldBeSuccess()

                    verify(exactly = 2) { holidayRepository.get(countrySet = KOREA_COUNTRY_SET) }
                    verify(exactly = 0) { repository.getHiddenKeySet() }
                    coVerifyOrder {
                        repository.submitHiddenKeySet(hiddenKeySet = emptySet())
                        repository.submitHiddenKeySet(hiddenKeySet = targetKeySet)
                        repository.submitHiddenKeySet(hiddenKeySet = setOf(workingDay.name))
                    }
                }
            }
        }

        Given("토글할 설정 공휴일 목록을 조회할 수 없다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val getSettingHolidayUseCase = getSettingHolidayUseCase(result = Result.failure(failure))
            val repository = mockk<HolidaySettingRepository>(relaxed = true)
            val toggleUseCase =
                ToggleHolidayVisibilityUseCase(
                    getSettingHolidayUseCase = getSettingHolidayUseCase,
                    holidaySettingRepository = repository,
                )

            When("토글을 실행한다") {
                Then("조회 실패 원인을 그대로 제공하고 저장하지 않는다") {
                    toggleUseCase(parameter = uniqueName(label = "toggle")).shouldBeFailure() shouldBeSameInstanceAs failure

                    verify(exactly = 1) { getSettingHolidayUseCase(parameter = Unit) }
                    coVerify(exactly = 0) {
                        repository.addHiddenKey(key = any())
                        repository.removeHiddenKey(key = any())
                        repository.submitHiddenKeySet(hiddenKeySet = any())
                    }
                    verify(exactly = 0) { repository.getHiddenKeySet() }
                }
            }
        }

        Given("일괄 선택할 공휴일 목록을 조회할 수 없다") {
            val failure = TestException(fixtureMonkey.giveMeOne())
            val holidayRepository =
                mockk<HolidayRepository>().also { repository ->
                    every { repository.get(countrySet = KOREA_COUNTRY_SET) } returns flow { throw failure }
                }
            val repository = mockk<HolidaySettingRepository>(relaxed = true)
            val deselectAllUseCase =
                DeselectAllHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )
            val selectDaysOffUseCase =
                SelectDaysOffHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )

            When("공휴일 목록이 필요한 일괄 선택을 실행한다") {
                Then("조회 실패 원인을 그대로 제공하고 저장하지 않는다") {
                    deselectAllUseCase(parameter = Unit).shouldBeFailure() shouldBeSameInstanceAs failure
                    selectDaysOffUseCase(parameter = Unit).shouldBeFailure() shouldBeSameInstanceAs failure

                    verify(exactly = 2) { holidayRepository.get(countrySet = KOREA_COUNTRY_SET) }
                    verify(exactly = 0) { repository.getHiddenKeySet() }
                    coVerify(exactly = 0) { repository.submitHiddenKeySet(hiddenKeySet = any()) }
                }
            }
        }

        Given("선택 상태를 저장할 수 없다") {
            val selected = holidaySetting(name = uniqueName(label = "selected"), isVisible = true)
            val deselected = holidaySetting(name = uniqueName(label = "deselected"), isVisible = false)
            val failure = TestException(fixtureMonkey.giveMeOne())
            val repository = mockk<HolidaySettingRepository>()
            coEvery { repository.addHiddenKey(key = any()) } throws failure
            coEvery { repository.removeHiddenKey(key = any()) } throws failure
            coEvery { repository.submitHiddenKeySet(hiddenKeySet = any()) } throws failure
            val selectToggleUseCase =
                ToggleHolidayVisibilityUseCase(
                    getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingList = listOf(selected)),
                    holidaySettingRepository = repository,
                )
            val deselectToggleUseCase =
                ToggleHolidayVisibilityUseCase(
                    getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingList = listOf(deselected)),
                    holidaySettingRepository = repository,
                )
            val getSettingHolidayUseCase =
                getSettingHolidayUseCase(holidaySettingList = listOf(selected, deselected))
            val holidayRepository =
                holidayRepository(
                    holidayList =
                        listOf(
                            holiday(name = selected.name, isHoliday = true),
                            holiday(name = deselected.name, isHoliday = false),
                        ),
                )
            val selectAllUseCase =
                SelectAllHolidayUseCase(
                    holidaySettingRepository = repository,
                )
            val deselectAllUseCase =
                DeselectAllHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )
            val selectDaysOffUseCase =
                SelectDaysOffHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository = holidayRepository,
                    holidaySettingRepository = repository,
                )

            When("각 선택 동작을 실행한다") {
                Then("저장 실패 원인을 그대로 제공한다") {
                    selectToggleUseCase(parameter = selected.name).shouldBeFailure() shouldBeSameInstanceAs failure
                    deselectToggleUseCase(parameter = deselected.name).shouldBeFailure() shouldBeSameInstanceAs failure
                    selectAllUseCase(parameter = Unit).shouldBeFailure() shouldBeSameInstanceAs failure
                    deselectAllUseCase(parameter = Unit).shouldBeFailure() shouldBeSameInstanceAs failure
                    selectDaysOffUseCase(parameter = Unit).shouldBeFailure() shouldBeSameInstanceAs failure

                    coVerify(exactly = 1) {
                        repository.addHiddenKey(key = selected.name)
                        repository.removeHiddenKey(key = deselected.name)
                    }
                    verify(exactly = 2) { holidayRepository.get(countrySet = KOREA_COUNTRY_SET) }
                    verify(exactly = 0) { repository.getHiddenKeySet() }
                    coVerify(exactly = 3) { repository.submitHiddenKeySet(hiddenKeySet = any()) }
                }
            }
        }
    })

private class TestException(
    message: String,
) : RuntimeException(message)

private fun getSettingHolidayUseCase(holidaySettingList: List<HolidaySetting>): GetSettingHolidayUseCase = getSettingHolidayUseCase(result = Result.success(holidaySettingList))

private fun getSettingHolidayUseCase(result: Result<List<HolidaySetting>>): GetSettingHolidayUseCase =
    mockk<GetSettingHolidayUseCase>().also { useCase ->
        every { useCase(parameter = Unit) } returns flowOf(result)
    }

private fun holidayRepository(holidayList: List<Holiday>): HolidayRepository =
    mockk<HolidayRepository>().also { repository ->
        every { repository.get(countrySet = KOREA_COUNTRY_SET) } returns flowOf(holidayList)
    }

private fun holiday(
    name: String,
    isHoliday: Boolean = fixtureMonkey.giveMeOne(),
): Holiday {
    val date = LocalDate(year = 2026, month = 1, day = 1)

    return Holiday(
        name = name,
        isHoliday = isHoliday,
        dateRange = date..date,
    )
}

private fun holidaySetting(
    name: String = uniqueName(label = "holiday"),
    isHoliday: Boolean = fixtureMonkey.giveMeOne(),
    isVisible: Boolean = fixtureMonkey.giveMeOne(),
): HolidaySetting =
    HolidaySetting(
        name = name,
        isHoliday = isHoliday,
        isVisible = isVisible,
    )

private fun uniqueName(label: String): String = "${fixtureMonkey.giveMeOne<String>()}-$label"
