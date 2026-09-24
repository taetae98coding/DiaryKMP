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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

        test("TC-SETTING-HOLIDAY-FEATURE-008 선택된 공휴일을 선택하면 해당 key를 전환한다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.holidaySetting(index = 0, isVisible = true)
                val holidaySettingList =
                    listOf(
                        target,
                        fixtureMonkey.holidaySetting(index = 1, isVisible = false),
                    )
                val toggleHolidayVisibilityUseCase = mockk<ToggleHolidayVisibilityUseCase>()
                coEvery { toggleHolidayVisibilityUseCase(parameter = target.key) } returns Result.success(Unit)
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

                    viewModel.toggleHoliday(key = target.key)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) {
                    toggleHolidayVisibilityUseCase(parameter = target.key)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-008 선택 해제된 공휴일을 선택하면 해당 key를 전환한다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.holidaySetting(index = 0, isVisible = false)
                val holidaySettingList =
                    listOf(
                        target,
                        fixtureMonkey.holidaySetting(index = 1, isVisible = true),
                    )
                val toggleHolidayVisibilityUseCase = mockk<ToggleHolidayVisibilityUseCase>()
                coEvery { toggleHolidayVisibilityUseCase(parameter = target.key) } returns Result.success(Unit)
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

                    viewModel.toggleHoliday(key = target.key)
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) {
                    toggleHolidayVisibilityUseCase(parameter = target.key)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-009 전체 선택 행동은 전용 UseCase를 실행한다") {
            runTest(mainDispatcher) {
                val selectAllHolidayUseCase = mockk<SelectAllHolidayUseCase>()
                coEvery { selectAllHolidayUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(emptyFlow()),
                        selectAllHolidayUseCase = selectAllHolidayUseCase,
                    )

                viewModel.selectAll()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    selectAllHolidayUseCase(parameter = Unit)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-010 전체 해제 행동은 전용 UseCase를 실행한다") {
            runTest(mainDispatcher) {
                val deselectAllHolidayUseCase = mockk<DeselectAllHolidayUseCase>()
                coEvery { deselectAllHolidayUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(emptyFlow()),
                        deselectAllHolidayUseCase = deselectAllHolidayUseCase,
                    )

                viewModel.deselectAll()
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    deselectAllHolidayUseCase(parameter = Unit)
                }
            }
        }

        test("TC-SETTING-HOLIDAY-FEATURE-011 쉬는 날만 선택 행동은 전용 UseCase를 실행한다") {
            runTest(mainDispatcher) {
                val selectDaysOffHolidayUseCase = mockk<SelectDaysOffHolidayUseCase>()
                coEvery { selectDaysOffHolidayUseCase(parameter = Unit) } returns Result.success(Unit)
                val viewModel =
                    settingHolidayViewModel(
                        getSettingHolidayUseCase = getSettingHolidayUseCase(emptyFlow()),
                        selectDaysOffHolidayUseCase = selectDaysOffHolidayUseCase,
                    )

                viewModel.selectDaysOff()
                advanceUntilIdle()

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

                    viewModel.toggleHoliday(key = unknownKey)
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

                    viewModel.toggleHoliday(key = fixtureMonkey.giveMeOne())
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
        key = "$index-${giveMeOne<String>()}",
        isHoliday = isHoliday,
        isVisible = isVisible,
    )
