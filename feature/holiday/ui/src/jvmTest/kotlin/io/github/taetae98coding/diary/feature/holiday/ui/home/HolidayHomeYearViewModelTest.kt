@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.holiday.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.UI_STOP_TIMEOUT_MILLIS
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayHomeYearViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-HOLIDAY-HOME-DATA-001 년도가 화면에 드러나면 그 년도와 앞뒤 년도의 공휴일을 동기화한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val fetchHolidayUseCase = successfulFetchHolidayUseCase()

                holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase).fetch()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchHolidayUseCase(parameter = year - 1) }
                coVerify(exactly = 1) { fetchHolidayUseCase(parameter = year) }
                coVerify(exactly = 1) { fetchHolidayUseCase(parameter = year + 1) }
            }
        }

        test("TC-HOLIDAY-HOME-DATA-002 다른 년도로 이동하면 이동한 년도 기준으로 동기화한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val fetchHolidayUseCase = successfulFetchHolidayUseCase()

                holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase).fetch()
                advanceUntilIdle()
                holidayHomeYearViewModel(year = year + 1, fetchHolidayUseCase = fetchHolidayUseCase).fetch()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchHolidayUseCase(parameter = year - 1) }
                coVerify(exactly = 2) { fetchHolidayUseCase(parameter = year) }
                coVerify(exactly = 2) { fetchHolidayUseCase(parameter = year + 1) }
                coVerify(exactly = 1) { fetchHolidayUseCase(parameter = year + 2) }
            }
        }

        test("TC-HOLIDAY-HOME-DATA-004 한 판단 대상 년도의 동기화가 실패해도 다른 대상 년도의 동기화를 계속한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = year - 1) } returns
                    Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                coEvery { fetchHolidayUseCase(parameter = year) } returns Result.success(providedHolidayList())
                coEvery { fetchHolidayUseCase(parameter = year + 1) } returns Result.success(providedHolidayList())

                holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase).fetch()
                advanceUntilIdle()

                coVerifyOrder {
                    fetchHolidayUseCase(parameter = year - 1)
                    fetchHolidayUseCase(parameter = year)
                    fetchHolidayUseCase(parameter = year + 1)
                }
            }
        }

        test("TC-HOLIDAY-HOME-DATA-009 한 년도의 동기화가 진행 중이어도 다른 년도의 동기화를 시작한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val completion = CompletableDeferred<Result<List<Holiday>>>()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
                coEvery { fetchHolidayUseCase(parameter = year - 1) } coAnswers { completion.await() }

                holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase).fetch()
                holidayHomeYearViewModel(year = year + 1, fetchHolidayUseCase = fetchHolidayUseCase).fetch()
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchHolidayUseCase(parameter = year + 2) }

                completion.complete(Result.success(providedHolidayList()))
                advanceUntilIdle()
            }
        }

        test("TC-HOLIDAY-HOME-DATA-012 년도가 다시 화면에 드러나면 동기화를 다시 요청한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val fetchHolidayUseCase = successfulFetchHolidayUseCase()
                val viewModel = holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase)
                viewModel.fetch()
                advanceUntilIdle()

                viewModel.fetch()
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchHolidayUseCase(parameter = year - 1) }
                coVerify(exactly = 2) { fetchHolidayUseCase(parameter = year) }
                coVerify(exactly = 2) { fetchHolidayUseCase(parameter = year + 1) }
            }
        }

        test("동기화가 끝나기 전에는 로딩 상태를 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val completion = CompletableDeferred<Result<List<Holiday>>>()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } coAnswers { completion.await() }
                val viewModel = holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    expectNoEvents()
                }

                completion.complete(Result.success(providedHolidayList()))
                advanceUntilIdle()
            }
        }

        test("모든 대상 년도의 동기화가 성공하면 조회한 황금연휴 목록을 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val goldenHolidayGroup = goldenHolidayGroup(year = year)
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns
                    flowOf(Result.success(listOf(goldenHolidayGroup)))
                val viewModel = holidayHomeYearViewModel(year = year, getGoldenHolidayUseCase = getGoldenHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(goldenHolidayGroup))
                }
            }
        }

        test("대상 년도 중 하나라도 동기화에 실패하면 오류 상태를 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
                coEvery { fetchHolidayUseCase(parameter = year + 1) } returns
                    Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Error
                }
            }
        }

        test("표시 년도의 공휴일이 제공되지 않으면 제공 없음 상태를 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
                coEvery { fetchHolidayUseCase(parameter = year) } returns Result.success(emptyList())
                val viewModel = holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.NotProvided
                }
            }
        }

        test("앞뒤 년도의 공휴일만 제공되지 않으면 황금연휴 목록을 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val goldenHolidayGroup = goldenHolidayGroup(year = year)
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(emptyList())
                coEvery { fetchHolidayUseCase(parameter = year) } returns Result.success(providedHolidayList())
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns
                    flowOf(Result.success(listOf(goldenHolidayGroup)))
                val viewModel =
                    holidayHomeYearViewModel(
                        year = year,
                        fetchHolidayUseCase = fetchHolidayUseCase,
                        getGoldenHolidayUseCase = getGoldenHolidayUseCase,
                    )
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(goldenHolidayGroup))
                }
            }
        }

        test("받아오지 못한 대상 년도가 있으면 표시 년도가 제공되지 않아도 오류 상태를 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
                coEvery { fetchHolidayUseCase(parameter = year) } returns Result.success(emptyList())
                coEvery { fetchHolidayUseCase(parameter = year - 1) } returns
                    Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Error
                }
            }
        }

        test("제공 없음 상태에서 다시 동기화하면 로딩 상태로 돌아간다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val goldenHolidayGroup = goldenHolidayGroup(year = year)
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
                coEvery { fetchHolidayUseCase(parameter = year) } returnsMany
                    listOf(
                        Result.success(emptyList()),
                        Result.success(providedHolidayList()),
                    )
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns
                    flowOf(Result.success(listOf(goldenHolidayGroup)))
                val viewModel =
                    holidayHomeYearViewModel(
                        year = year,
                        fetchHolidayUseCase = fetchHolidayUseCase,
                        getGoldenHolidayUseCase = getGoldenHolidayUseCase,
                    )
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.NotProvided

                    viewModel.fetch()

                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(goldenHolidayGroup))
                }
            }
        }

        test("재시도하면 다시 동기화하고 성공하면 목록 상태가 된다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val goldenHolidayGroup = goldenHolidayGroup(year = year)
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
                coEvery { fetchHolidayUseCase(parameter = year - 1) } returnsMany
                    listOf(
                        Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                        Result.success(providedHolidayList()),
                    )
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns
                    flowOf(Result.success(listOf(goldenHolidayGroup)))
                val viewModel =
                    holidayHomeYearViewModel(
                        year = year,
                        fetchHolidayUseCase = fetchHolidayUseCase,
                        getGoldenHolidayUseCase = getGoldenHolidayUseCase,
                    )
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Error

                    viewModel.fetch()

                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(goldenHolidayGroup))
                }

                coVerify(exactly = 2) { fetchHolidayUseCase(parameter = year - 1) }
            }
        }

        test("다시 동기화하는 동안 이미 로딩된 목록을 유지한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val goldenHolidayGroup = goldenHolidayGroup(year = year)
                val completion = CompletableDeferred<Result<List<Holiday>>>()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(providedHolidayList())
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns
                    flowOf(Result.success(listOf(goldenHolidayGroup)))
                val viewModel =
                    holidayHomeYearViewModel(
                        year = year,
                        fetchHolidayUseCase = fetchHolidayUseCase,
                        getGoldenHolidayUseCase = getGoldenHolidayUseCase,
                    )
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(goldenHolidayGroup))

                    coEvery { fetchHolidayUseCase(parameter = any()) } coAnswers { completion.await() }
                    viewModel.fetch()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                completion.complete(Result.success(providedHolidayList()))
                advanceUntilIdle()
            }
        }

        test("연차 개수가 바뀌면 바뀐 연차 개수로 황금연휴를 다시 조회한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val goldenHolidayGroup = goldenHolidayGroup(year = year)
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } answers {
                    val parameter = firstArg<GetGoldenHolidayUseCase.Parameter>()

                    flowOf(Result.success(if (parameter.annualLeaveCount > 0) listOf(goldenHolidayGroup) else emptyList()))
                }
                val viewModel = holidayHomeYearViewModel(year = year, getGoldenHolidayUseCase = getGoldenHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded()

                    viewModel.updateAnnualLeaveCount(annualLeaveCount = 1)

                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(goldenHolidayGroup))
                }
            }
        }

        test("조회 중인 황금연휴가 바뀌면 바뀐 목록을 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val goldenHolidayGroup = goldenHolidayGroup(year = year)
                val changedGoldenHolidayGroup = goldenHolidayGroup(year = year)
                val goldenHolidayGroupFlow = MutableStateFlow(listOf(goldenHolidayGroup))
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns
                    goldenHolidayGroupFlow.map { groupList -> Result.success(groupList) }
                val viewModel = holidayHomeYearViewModel(year = year, getGoldenHolidayUseCase = getGoldenHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(goldenHolidayGroup))

                    goldenHolidayGroupFlow.value = listOf(changedGoldenHolidayGroup)

                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = listOf(changedGoldenHolidayGroup))
                }
            }
        }

        test("황금연휴 조회에 실패하면 알리지 않고 공휴일 없이 계산한 빈 목록을 표시한다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns
                    flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
                val viewModel = holidayHomeYearViewModel(year = year, getGoldenHolidayUseCase = getGoldenHolidayUseCase)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = emptyList())
                }
            }
        }

        test("TC-HOLIDAY-HOME-FEATURE-060 기기 지역을 바꾸고 5초가 지나기 전에 돌아오면 목록은 이전 지역 기준으로 남는다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val previousRegionList = listOf(goldenHolidayGroup(year = year))
                val changedRegionList = listOf(goldenHolidayGroup(year = year))
                val getGoldenHolidayUseCase = regionChangingGetGoldenHolidayUseCase(previousRegionList, changedRegionList)
                val viewModel = holidayHomeYearViewModel(year = year, getGoldenHolidayUseCase = getGoldenHolidayUseCase)
                viewModel.fetch()
                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = previousRegionList)
                }

                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS - 1)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = previousRegionList)

                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-HOLIDAY-HOME-FEATURE-061 기기 지역을 바꾸고 5초가 지난 뒤 돌아오면 바뀐 지역 기준으로 목록이 다시 표시된다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val previousRegionList = listOf(goldenHolidayGroup(year = year))
                val changedRegionList = listOf(goldenHolidayGroup(year = year))
                val getGoldenHolidayUseCase = regionChangingGetGoldenHolidayUseCase(previousRegionList, changedRegionList)
                val viewModel = holidayHomeYearViewModel(year = year, getGoldenHolidayUseCase = getGoldenHolidayUseCase)
                viewModel.fetch()
                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = previousRegionList)
                }

                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS + 1)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = previousRegionList)
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = changedRegionList)
                }
            }
        }

        test("TC-HOLIDAY-HOME-FEATURE-062 바뀐 지역에 적용 국가가 없으면 5초가 지나기 전에 돌아와도 제공 없음 안내가 표시된다") {
            runTest(mainDispatcher) {
                val year = randomYear()
                val previousRegionList = listOf(goldenHolidayGroup(year = year))
                var isRegionChanged = false
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = any()) } coAnswers {
                    if (isRegionChanged) Result.success(emptyList()) else Result.success(providedHolidayList())
                }
                val getGoldenHolidayUseCase = mockk<GetGoldenHolidayUseCase>()
                every { getGoldenHolidayUseCase(parameter = any()) } returns flowOf(Result.success(previousRegionList))
                val viewModel =
                    holidayHomeYearViewModel(year = year, fetchHolidayUseCase = fetchHolidayUseCase, getGoldenHolidayUseCase = getGoldenHolidayUseCase)
                viewModel.fetch()
                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loading
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = previousRegionList)
                }

                isRegionChanged = true
                advanceTimeBy(UI_STOP_TIMEOUT_MILLIS - 1)
                viewModel.fetch()

                viewModel.uiState.test {
                    awaitItem() shouldBe HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = previousRegionList)
                    awaitItem() shouldBe HolidayHomeYearUiState.NotProvided
                }
            }
        }
    }
}

private fun holidayHomeYearViewModel(
    year: Int,
    fetchHolidayUseCase: FetchHolidayUseCase = successfulFetchHolidayUseCase(),
    getGoldenHolidayUseCase: GetGoldenHolidayUseCase = emptyGetGoldenHolidayUseCase(),
): HolidayHomeYearViewModel =
    HolidayHomeYearViewModel(
        year = year,
        fetchHolidayUseCase = fetchHolidayUseCase,
        getGoldenHolidayUseCase = getGoldenHolidayUseCase,
    )

// 기기 지역은 조회를 시작할 때 한 번 읽히므로, 두 번째 조회부터 바뀐 지역의 결과를 돌려주는 것으로 지역 변경을 흉내 낸다.
private fun regionChangingGetGoldenHolidayUseCase(
    previousRegionList: List<GoldenHolidayGroup>,
    changedRegionList: List<GoldenHolidayGroup>,
): GetGoldenHolidayUseCase =
    mockk<GetGoldenHolidayUseCase>().also { useCase ->
        every { useCase(parameter = any()) } returnsMany
            listOf(
                flowOf(Result.success(previousRegionList)),
                flowOf(Result.success(changedRegionList)),
            )
    }

private fun providedHolidayList(): List<Holiday> = listOf(holiday(year = randomYear()))

private fun successfulFetchHolidayUseCase(): FetchHolidayUseCase =
    mockk<FetchHolidayUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(providedHolidayList())
    }

private fun emptyGetGoldenHolidayUseCase(): GetGoldenHolidayUseCase =
    mockk<GetGoldenHolidayUseCase>().also { useCase ->
        every { useCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
    }

private fun goldenHolidayGroup(year: Int): GoldenHolidayGroup {
    val start = randomDate(year = year)

    return GoldenHolidayGroup(
        optionList =
            listOf(
                GoldenHoliday(
                    dateRange = start..start.plus(2, DateTimeUnit.DAY),
                    holidayList = listOf(holiday(year = year)),
                    annualLeaveDateRangeList = emptyList(),
                ),
            ),
    )
}

private fun holiday(year: Int): Holiday {
    val start = randomDate(year = year)

    return Holiday(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = true,
        dateRange = start..start,
    )
}

private fun randomDate(year: Int): LocalDate =
    LocalDate(
        year = year,
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )

private fun randomYear(): Int = 2_000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 1_000u).toInt()
