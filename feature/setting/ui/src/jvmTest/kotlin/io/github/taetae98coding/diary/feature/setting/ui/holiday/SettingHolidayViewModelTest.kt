@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.setting.ui.holiday

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountrySetting
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.domain.holiday.usecase.DeselectAllHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetHolidayCountrySettingUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetSettingHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectAllHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectDaysOffHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayCountryOptionUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayVisibilityUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SettingHolidayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SETTING-HOLIDAY-FEATURE-006 저장된 공휴일 목록과 선택 상태를 제공한다") {
            runTest(mainDispatcher) {
                val holidaySettingList =
                    listOf(
                        fixtureMonkey.holidaySetting(index = 0, isVisible = true),
                        fixtureMonkey.holidaySetting(index = 1, isVisible = false),
                        fixtureMonkey.holidaySetting(index = 2, isVisible = true),
                    )
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase =
                            getSettingHolidayUseCase(
                                flowOf(Result.success(holidaySettingList)),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading

                    val loaded = awaitItem() as SettingHolidayUiState.Loaded
                    loaded.holidaySettingList shouldBe holidaySettingList
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-008 공휴일을 선택하면 그 이름의 전환을 한 번 요청하고 저장된 결과가 반영되면 목표 상태로 바뀐다") {
            listOf(true, false).forEach { isVisible ->
                runTest(mainDispatcher) {
                    val target = fixtureMonkey.holidaySetting(index = 0, isVisible = isVisible)
                    val other = fixtureMonkey.holidaySetting(index = 1)
                    val holidaySettingFlow = MutableStateFlow(Result.success(listOf(target, other)))
                    val toggleHolidayVisibilityUseCase = mockk<ToggleHolidayVisibilityUseCase>()
                    coEvery { toggleHolidayVisibilityUseCase(parameter = target.name) } coAnswers {
                        holidaySettingFlow.value = Result.success(listOf(target.copy(isVisible = !isVisible), other))
                        Result.success(Unit)
                    }
                    val viewModel =
                        settingHolidayViewModel(
                            getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingFlow),
                            toggleHolidayVisibilityUseCase = toggleHolidayVisibilityUseCase,
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingHolidayUiState.Loading
                        awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = DEFAULT_COUNTRY_SETTING, holidaySettingList = listOf(target, other))

                        viewModel.toggleHoliday(name = target.name)

                        awaitItem() shouldBe
                            SettingHolidayUiState.Loaded(
                                countrySetting = DEFAULT_COUNTRY_SETTING,
                                holidaySettingList = listOf(target.copy(isVisible = !isVisible), other),
                            )
                    }

                    coVerify(exactly = 1) {
                        toggleHolidayVisibilityUseCase(parameter = target.name)
                    }
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-009 전체 선택을 실행하고 저장된 결과가 반영되면 모든 항목이 선택 상태가 된다") {
            runTest(mainDispatcher) {
                val holidaySettingList = listOf(fixtureMonkey.holidaySetting(index = 0, isVisible = true), fixtureMonkey.holidaySetting(index = 1, isVisible = false))
                val holidaySettingFlow = MutableStateFlow(Result.success(holidaySettingList))
                val selectAllHolidayUseCase = mockk<SelectAllHolidayUseCase>()
                coEvery { selectAllHolidayUseCase(parameter = Unit) } coAnswers {
                    holidaySettingFlow.value = Result.success(holidaySettingList.map { setting -> setting.copy(isVisible = true) })
                    Result.success(Unit)
                }
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingFlow),
                        selectAllHolidayUseCase = selectAllHolidayUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    awaitItem()

                    viewModel.selectAll()

                    (awaitItem() as SettingHolidayUiState.Loaded).holidaySettingList.map { setting -> setting.isVisible } shouldBe listOf(true, true)
                }

                coVerify(exactly = 1) {
                    selectAllHolidayUseCase(parameter = Unit)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-010 전체 해제를 실행하고 저장된 결과가 반영되면 모든 항목이 선택 해제 상태가 된다") {
            runTest(mainDispatcher) {
                val holidaySettingList = listOf(fixtureMonkey.holidaySetting(index = 0, isVisible = true), fixtureMonkey.holidaySetting(index = 1, isVisible = false))
                val holidaySettingFlow = MutableStateFlow(Result.success(holidaySettingList))
                val deselectAllHolidayUseCase = mockk<DeselectAllHolidayUseCase>()
                coEvery { deselectAllHolidayUseCase(parameter = Unit) } coAnswers {
                    holidaySettingFlow.value = Result.success(holidaySettingList.map { setting -> setting.copy(isVisible = false) })
                    Result.success(Unit)
                }
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingFlow),
                        deselectAllHolidayUseCase = deselectAllHolidayUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    awaitItem()

                    viewModel.deselectAll()

                    (awaitItem() as SettingHolidayUiState.Loaded).holidaySettingList.map { setting -> setting.isVisible } shouldBe listOf(false, false)
                }

                coVerify(exactly = 1) {
                    deselectAllHolidayUseCase(parameter = Unit)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-011 쉬는 날만 선택을 실행하고 저장된 결과가 반영되면 쉬는 날 항목만 선택 상태가 된다") {
            runTest(mainDispatcher) {
                val holidaySettingList =
                    listOf(
                        fixtureMonkey.holidaySetting(index = 0, isHoliday = true, isVisible = false),
                        fixtureMonkey.holidaySetting(index = 1, isHoliday = false, isVisible = true),
                    )
                val holidaySettingFlow = MutableStateFlow(Result.success(holidaySettingList))
                val selectDaysOffHolidayUseCase = mockk<SelectDaysOffHolidayUseCase>()
                coEvery { selectDaysOffHolidayUseCase(parameter = Unit) } coAnswers {
                    holidaySettingFlow.value = Result.success(holidaySettingList.map { setting -> setting.copy(isVisible = setting.isHoliday) })
                    Result.success(Unit)
                }
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingFlow),
                        selectDaysOffHolidayUseCase = selectDaysOffHolidayUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    awaitItem()

                    viewModel.selectDaysOff()

                    (awaitItem() as SettingHolidayUiState.Loaded).holidaySettingList.map { setting -> setting.isVisible } shouldBe listOf(true, false)
                }

                coVerify(exactly = 1) {
                    selectDaysOffHolidayUseCase(parameter = Unit)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-018 원본 목록과 선택 상태가 바뀌면 최신 상태를 제공한다") {
            runTest(mainDispatcher) {
                val first = fixtureMonkey.holidaySetting(index = 0, isVisible = true)
                val initialList = listOf(first)
                val second = fixtureMonkey.holidaySetting(index = 1, isVisible = true)
                val expandedList = listOf(first, second)
                val visibilityUpdatedList = listOf(first.copy(isVisible = false), second)
                val holidaySettingFlow = MutableStateFlow(Result.success(initialList))
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingFlow),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading

                    val initialState = awaitItem() as SettingHolidayUiState.Loaded
                    initialState.holidaySettingList shouldBe initialList

                    holidaySettingFlow.value = Result.success(expandedList)

                    val expandedState = awaitItem() as SettingHolidayUiState.Loaded
                    expandedState.holidaySettingList shouldBe expandedList

                    holidaySettingFlow.value = Result.success(visibilityUpdatedList)

                    val visibilityUpdatedState = awaitItem() as SettingHolidayUiState.Loaded
                    visibilityUpdatedState.holidaySettingList shouldBe visibilityUpdatedList
                }
            }
        }

        test("목록에 없는 key도 해석하지 않고 전용 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val holidaySettingList = listOf(fixtureMonkey.holidaySetting(index = 0))
                val unknownKey = "unknown-${fixtureMonkey.giveMeOne<String>()}"
                val toggleHolidayVisibilityUseCase = mockk<ToggleHolidayVisibilityUseCase>()
                coEvery {
                    toggleHolidayVisibilityUseCase(parameter = unknownKey)
                } returns Result.success(Unit)
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase =
                            getSettingHolidayUseCase(
                                flowOf(Result.success(holidaySettingList)),
                            ),
                        toggleHolidayVisibilityUseCase = toggleHolidayVisibilityUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = DEFAULT_COUNTRY_SETTING, holidaySettingList = holidaySettingList)

                    viewModel.toggleHoliday(name = unknownKey)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) {
                    toggleHolidayVisibilityUseCase(parameter = unknownKey)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-032 확인 중에도 일괄 선택을 실행한다") {
            runTest(mainDispatcher) {
                val selectAllHolidayUseCase = mockk<SelectAllHolidayUseCase>()
                val deselectAllHolidayUseCase = mockk<DeselectAllHolidayUseCase>()
                val selectDaysOffHolidayUseCase = mockk<SelectDaysOffHolidayUseCase>()
                coEvery { selectAllHolidayUseCase(parameter = Unit) } returns Result.success(Unit)
                coEvery { deselectAllHolidayUseCase(parameter = Unit) } returns Result.success(Unit)
                coEvery { selectDaysOffHolidayUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase =
                            getSettingHolidayUseCase(
                                emptyFlow<Result<List<HolidaySetting>>>(),
                            ),
                        selectAllHolidayUseCase = selectAllHolidayUseCase,
                        deselectAllHolidayUseCase = deselectAllHolidayUseCase,
                        selectDaysOffHolidayUseCase = selectDaysOffHolidayUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading

                    viewModel.selectAll()
                    viewModel.deselectAll()
                    viewModel.selectDaysOff()
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) {
                    selectAllHolidayUseCase(parameter = Unit)
                    deselectAllHolidayUseCase(parameter = Unit)
                    selectDaysOffHolidayUseCase(parameter = Unit)
                }
            }
        }

        test("확인 중에도 공휴일 항목 선택은 해석하지 않고 전용 UseCase에 전달한다") {
            runTest(mainDispatcher) {
                val toggleHolidayVisibilityUseCase = mockk<ToggleHolidayVisibilityUseCase>()
                coEvery {
                    toggleHolidayVisibilityUseCase(parameter = any())
                } returns Result.success(Unit)
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase =
                            getSettingHolidayUseCase(
                                emptyFlow<Result<List<HolidaySetting>>>(),
                            ),
                        toggleHolidayVisibilityUseCase = toggleHolidayVisibilityUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading

                    viewModel.toggleHoliday(name = fixtureMonkey.giveMeOne())
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) {
                    toggleHolidayVisibilityUseCase(parameter = any())
                }
            }
        }

        test("국가 설정과 공휴일 목록이 모두 확인되어야 목록 상태를 제공한다") {
            runTest(mainDispatcher) {
                val holidaySettingList = listOf(fixtureMonkey.holidaySetting(index = 0))
                val countrySettingFlow = MutableStateFlow<Result<HolidayCountrySetting>>(Result.failure(IllegalStateException()))
                val viewModel =
                    settingHolidayViewModel(
                        getHolidayCountrySettingUseCase = getHolidayCountrySettingUseCase(countrySettingFlow),
                        getSettingHolidayUseCase =
                            getSettingHolidayUseCase(
                                flowOf(Result.success(holidaySettingList)),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()

                    countrySettingFlow.value = Result.success(DEFAULT_COUNTRY_SETTING)

                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = DEFAULT_COUNTRY_SETTING, holidaySettingList = holidaySettingList)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-042 설정이나 공휴일을 읽지 못하면 확인 중 상태를 유지한다") {
            val caseList =
                listOf(
                    Result.failure<HolidayCountrySetting>(IllegalStateException("read error")) to
                        Result.success(listOf(fixtureMonkey.holidaySetting(index = 0))),
                    Result.success(DEFAULT_COUNTRY_SETTING) to Result.failure<List<HolidaySetting>>(IllegalStateException("read error")),
                )

            caseList.forEach { (countrySettingResult, holidaySettingListResult) ->
                runTest(mainDispatcher) {
                    val viewModel =
                        settingHolidayViewModel(
                            getHolidayCountrySettingUseCase = getHolidayCountrySettingUseCase(flowOf(countrySettingResult)),
                            getSettingHolidayUseCase = getSettingHolidayUseCase(flowOf(holidaySettingListResult)),
                        )

                    viewModel.uiState.test {
                        awaitItem() shouldBe SettingHolidayUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-035 국가 선택지 변경은 해당 선택지로 전용 UseCase를 실행한다") {
            runTest(mainDispatcher) {
                val toggleHolidayCountryOptionUseCase = mockk<ToggleHolidayCountryOptionUseCase>()
                coEvery { toggleHolidayCountryOptionUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(emptyFlow()),
                        toggleHolidayCountryOptionUseCase = toggleHolidayCountryOptionUseCase,
                    )

                HolidayCountryOption.entries.forEach { option -> viewModel.toggleCountryOption(option = option) }
                advanceUntilIdle()

                HolidayCountryOption.entries.forEach { option ->
                    coVerify(exactly = 1) { toggleHolidayCountryOptionUseCase(parameter = option) }
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-038 국가 설정이 바뀌면 바뀐 설정과 목록을 제공한다") {
            runTest(mainDispatcher) {
                val koreaList = listOf(fixtureMonkey.holidaySetting(index = 0))
                val bothList = koreaList + fixtureMonkey.holidaySetting(index = 1)
                val koreaSetting = HolidayCountrySetting(selectedOptionSet = setOf(HolidayCountryOption.KOREA), deviceCountry = null)
                val bothSetting = koreaSetting.copy(selectedOptionSet = setOf(HolidayCountryOption.KOREA, HolidayCountryOption.UNITED_STATES))
                val countrySettingFlow = MutableStateFlow(Result.success(koreaSetting))
                val holidaySettingFlow = MutableStateFlow(Result.success(koreaList))
                val viewModel =
                    settingHolidayViewModel(
                        getHolidayCountrySettingUseCase = getHolidayCountrySettingUseCase(countrySettingFlow),
                        getSettingHolidayUseCase = getSettingHolidayUseCase(holidaySettingFlow),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = koreaSetting, holidaySettingList = koreaList)

                    countrySettingFlow.value = Result.success(bothSetting)
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = bothSetting, holidaySettingList = koreaList)

                    holidaySettingFlow.value = Result.success(bothList)
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = bothSetting, holidaySettingList = bothList)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-046 기기 지역을 바꾸고 5초가 지나기 전에 돌아오면 이전 지역 기준을 그대로 보여 준다") {
            runTest(mainDispatcher) {
                val koreaList = listOf(fixtureMonkey.holidaySetting(index = 0))
                val unitedStatesList = listOf(fixtureMonkey.holidaySetting(index = 1))
                var deviceCountry = HolidayCountry.KOREA
                val viewModel =
                    settingHolidayViewModel(
                        getHolidayCountrySettingUseCase = deviceCountryReadingUseCase { deviceCountry },
                        getSettingHolidayUseCase =
                            deviceCountryReadingSettingHolidayUseCase {
                                if (deviceCountry == HolidayCountry.KOREA) koreaList else unitedStatesList
                            },
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = DEFAULT_COUNTRY_SETTING, holidaySettingList = koreaList)
                }
                deviceCountry = HolidayCountry.UNITED_STATES
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS - 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = DEFAULT_COUNTRY_SETTING, holidaySettingList = koreaList)

                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-047 기기 지역을 바꾸고 5초가 지난 뒤 돌아오면 바뀐 지역으로 다시 표시한다") {
            runTest(mainDispatcher) {
                val koreaList = listOf(fixtureMonkey.holidaySetting(index = 0))
                val unitedStatesList = listOf(fixtureMonkey.holidaySetting(index = 1))
                var deviceCountry = HolidayCountry.KOREA
                val viewModel =
                    settingHolidayViewModel(
                        getHolidayCountrySettingUseCase = deviceCountryReadingUseCase { deviceCountry },
                        getSettingHolidayUseCase =
                            deviceCountryReadingSettingHolidayUseCase {
                                if (deviceCountry == HolidayCountry.KOREA) koreaList else unitedStatesList
                            },
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loading
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = DEFAULT_COUNTRY_SETTING, holidaySettingList = koreaList)
                }
                deviceCountry = HolidayCountry.UNITED_STATES
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS + 1)

                viewModel.uiState.test {
                    awaitItem() shouldBe SettingHolidayUiState.Loaded(countrySetting = DEFAULT_COUNTRY_SETTING, holidaySettingList = koreaList)
                    awaitItem() shouldBe
                        SettingHolidayUiState.Loaded(
                            countrySetting = DEFAULT_COUNTRY_SETTING.copy(deviceCountry = HolidayCountry.UNITED_STATES),
                            holidaySettingList = unitedStatesList,
                        )
                }
            }
        }
    }
}

private val DEFAULT_COUNTRY_SETTING: HolidayCountrySetting =
    HolidayCountrySetting(
        selectedOptionSet = setOf(HolidayCountryOption.DEVICE),
        deviceCountry = HolidayCountry.KOREA,
    )

private fun getHolidayCountrySettingUseCase(flow: Flow<Result<HolidayCountrySetting>> = flowOf(Result.success(DEFAULT_COUNTRY_SETTING))): GetHolidayCountrySettingUseCase =
    mockk<GetHolidayCountrySettingUseCase>().also { useCase ->
        every { useCase(Unit) } returns flow
    }

// 기기 지역은 조회를 시작할 때마다 새로 읽힌다.
private fun deviceCountryReadingUseCase(deviceCountry: () -> HolidayCountry): GetHolidayCountrySettingUseCase =
    mockk<GetHolidayCountrySettingUseCase>().also { useCase ->
        every { useCase(Unit) } returns flow { emit(Result.success(DEFAULT_COUNTRY_SETTING.copy(deviceCountry = deviceCountry()))) }
    }

// 공휴일 목록도 조회를 시작할 때 기기 지역을 한 번 읽으므로, 조회를 시작할 때마다 그 시점 지역의 목록을 돌려준다.
private fun deviceCountryReadingSettingHolidayUseCase(holidaySettingList: () -> List<HolidaySetting>): GetSettingHolidayUseCase =
    mockk<GetSettingHolidayUseCase>().also { useCase ->
        every { useCase(Unit) } returns flow { emit(Result.success(holidaySettingList())) }
    }

private fun getSettingHolidayUseCase(flow: Flow<Result<List<HolidaySetting>>>): GetSettingHolidayUseCase =
    mockk<GetSettingHolidayUseCase>().also { useCase ->
        every { useCase(Unit) } returns flow
    }

private fun settingHolidayViewModel(
    getSettingHolidayUseCase: GetSettingHolidayUseCase,
    getHolidayCountrySettingUseCase: GetHolidayCountrySettingUseCase = getHolidayCountrySettingUseCase(),
    toggleHolidayCountryOptionUseCase: ToggleHolidayCountryOptionUseCase = mockk(),
    toggleHolidayVisibilityUseCase: ToggleHolidayVisibilityUseCase = mockk(),
    selectAllHolidayUseCase: SelectAllHolidayUseCase = mockk(),
    deselectAllHolidayUseCase: DeselectAllHolidayUseCase = mockk(),
    selectDaysOffHolidayUseCase: SelectDaysOffHolidayUseCase = mockk(),
): SettingHolidayViewModel =
    SettingHolidayViewModel(
        getHolidayCountrySettingUseCase = getHolidayCountrySettingUseCase,
        getSettingHolidayUseCase = getSettingHolidayUseCase,
        toggleHolidayCountryOptionUseCase = toggleHolidayCountryOptionUseCase,
        toggleHolidayVisibilityUseCase = toggleHolidayVisibilityUseCase,
        selectAllHolidayUseCase = selectAllHolidayUseCase,
        deselectAllHolidayUseCase = deselectAllHolidayUseCase,
        selectDaysOffHolidayUseCase = selectDaysOffHolidayUseCase,
    )

private fun FixtureMonkey.holidaySetting(
    index: Int,
    isHoliday: Boolean = giveMeOne(),
    isVisible: Boolean = giveMeOne(),
): HolidaySetting =
    giveMeOne<HolidaySetting>().copy(
        name = "$index-${giveMeOne<String>()}",
        isHoliday = isHoliday,
        isVisible = isVisible,
    )
