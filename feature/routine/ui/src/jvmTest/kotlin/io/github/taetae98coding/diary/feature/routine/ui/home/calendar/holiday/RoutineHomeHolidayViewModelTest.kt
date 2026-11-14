@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.testing.holiday.holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

class RoutineHomeHolidayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-3f5d4d91-4ba0-4b35-9151-e1acbb05c55c 표시 중인 달에 필요한 연도마다 공휴일 동기화를 요청한다") {
            runTest(mainDispatcher) {
                val yearListByYearMonth =
                    mapOf(
                        YearMonth(year = 2026, month = Month.JULY) to listOf(2026),
                        YearMonth(year = 2026, month = Month.JANUARY) to listOf(2025, 2026),
                        YearMonth(year = 2026, month = Month.FEBRUARY) to listOf(2025, 2026),
                        YearMonth(year = 2026, month = Month.NOVEMBER) to listOf(2026, 2027),
                        YearMonth(year = 2026, month = Month.DECEMBER) to listOf(2026, 2027),
                    )

                yearListByYearMonth.forEach { (yearMonth, yearList) ->
                    val useCase = successfulFetchHolidayUseCase()
                    val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                    viewModel.fetch(yearMonth = yearMonth)
                    advanceUntilIdle()

                    yearList.forEach { year -> coVerify(exactly = 1) { useCase(parameter = year) } }
                    coVerify(exactly = yearList.size) { useCase(parameter = any()) }
                }
            }
        }

        test("TC-badc63d3-8d62-4623-b3d2-6ae72d3c1398 동기화가 끝난 뒤 다음 해 1월로 이동하면 두 연도의 동기화를 새로 요청한다") {
            runTest(mainDispatcher) {
                val useCase = successfulFetchHolidayUseCase()
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)
                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.DECEMBER))
                advanceUntilIdle()
                coVerify(exactly = 1) { useCase(parameter = 2026) }
                coVerify(exactly = 1) { useCase(parameter = 2027) }

                viewModel.fetch(yearMonth = YearMonth(year = 2027, month = Month.JANUARY))
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(parameter = 2026) }
                coVerify(exactly = 2) { useCase(parameter = 2027) }
            }
        }

        test("TC-ff3219bf-a32c-40e7-8d8b-71ed3ddc16e0 진행 중인 연도는 다시 요청하지 않고 기다리지 않은 채 다른 연도를 요청한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<List<Holiday>>>()
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = 2026) } coAnswers { completion.await() }
                coEvery { useCase(parameter = 2027) } returns Result.success(emptyList())
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)
                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))
                advanceUntilIdle()

                viewModel.fetch(yearMonth = YearMonth(year = 2027, month = Month.JANUARY))
                advanceUntilIdle()

                completion.isCompleted shouldBe false
                coVerify(exactly = 1) { useCase(parameter = 2026) }
                coVerify(exactly = 1) { useCase(parameter = 2027) }

                completion.complete(Result.success(emptyList()))
                advanceUntilIdle()
            }
        }

        test("TC-90d5f4db-fa74-483b-9b4b-988d84dc6669 한 연도의 동기화가 실패해도 다른 대상 연도의 동기화를 계속한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = 2026) } returns Result.failure(IllegalStateException(Arb.string().next()))
                coEvery { useCase(parameter = 2027) } returns Result.success(emptyList())
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)
                backgroundScope.launch { viewModel.uiState.collect {} }

                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.DECEMBER))
                advanceUntilIdle()

                coVerifyOrder {
                    useCase(parameter = 2026)
                    useCase(parameter = 2027)
                }
                viewModel.uiState.value shouldBe RoutineHomeHolidayUiState()
            }
        }

        test("TC-d5c9a29d-b5d5-432a-88c9-4a97ea7c1e53 공휴일 동기화가 실행되는 동안 진행 중임을 알리고 끝나면 거둔다") {
            runTest(mainDispatcher) {
                val firstCompletion = CompletableDeferred<Result<List<Holiday>>>()
                val secondCompletion = CompletableDeferred<Result<List<Holiday>>>()
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = 2026) } coAnswers { firstCompletion.await() }
                coEvery { useCase(parameter = 2027) } coAnswers { secondCompletion.await() }
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)
                backgroundScope.launch { viewModel.uiState.collect {} }
                runCurrent()
                viewModel.uiState.value.isFetching shouldBe false

                // 달력 보기가 표시되어 시작한 동기화와 달이 바뀌어 시작한 동기화를 차례로 겹쳐 둔다.
                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))
                advanceUntilIdle()
                viewModel.uiState.value.isFetching shouldBe true

                viewModel.fetch(yearMonth = YearMonth(year = 2027, month = Month.MARCH))
                firstCompletion.complete(Result.success(emptyList()))
                advanceUntilIdle()
                viewModel.uiState.value.isFetching shouldBe true

                secondCompletion.complete(Result.success(emptyList()))
                advanceUntilIdle()
                viewModel.uiState.value.isFetching shouldBe false
            }
        }

        test("TC-d1fe881f-5cc1-4f0b-b7a1-7339ea88c2ce 표시 대상 연도의 공휴일만 표시한다") {
            runTest(mainDispatcher) {
                val previousYearHoliday = Arb.holiday(year = 2025, isHoliday = true).next()
                val holiday = Arb.holiday(year = 2026, isHoliday = true).next()
                val nextYearHoliday = Arb.holiday(year = 2027, isHoliday = true).next()
                val expectedByMonth =
                    listOf(
                        Month.JULY to listOf(holiday),
                        Month.JANUARY to listOf(previousYearHoliday, holiday),
                        Month.FEBRUARY to listOf(previousYearHoliday, holiday),
                        Month.NOVEMBER to listOf(holiday, nextYearHoliday),
                        Month.DECEMBER to listOf(holiday, nextYearHoliday),
                    )

                expectedByMonth.forEach { (month, expectedHolidayList) ->
                    val viewModel =
                        holidayViewModel(
                            getCalendarHolidayUseCase =
                                getCalendarHolidayUseCase(
                                    2025 to Result.success(listOf(previousYearHoliday)),
                                    2026 to Result.success(listOf(holiday)),
                                    2027 to Result.success(listOf(nextYearHoliday)),
                                ),
                        )

                    viewModel.fetch(yearMonth = YearMonth(year = 2026, month = month))

                    viewModel.holidayListFlow().test {
                        awaitItem() shouldBe emptyList()
                        awaitItem() shouldBe expectedHolidayList
                    }
                }
            }
        }

        test("TC-ed96a045-837e-4f19-9c3a-95312032f3c6 대상 연도에 저장된 공휴일을 표시하고 바뀌면 바뀐 공휴일을 표시한다") {
            runTest(mainDispatcher) {
                val holiday = holiday(name = CONSTITUTION_DAY_NAME, date = LocalDate(year = 2026, month = Month.JULY, day = 17))
                val changedHoliday = holiday(name = TEMPORARY_HOLIDAY_NAME, date = LocalDate(year = 2026, month = Month.JULY, day = 20))
                val holidayFlow = MutableStateFlow(listOf(holiday))
                val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getCalendarHolidayUseCase(parameter = 2026) } returns holidayFlow.map { holidayList -> Result.success(holidayList) }
                val viewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase)

                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))

                viewModel.holidayListFlow().test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(holiday)

                    holidayFlow.value = listOf(changedHoliday)

                    awaitItem() shouldBe listOf(changedHoliday)
                }
            }
        }

        test("TC-83a0068c-783e-4745-bf07-e2136d93ef45 대상 연도에 공휴일이 없거나 조회에 실패하면 공휴일 없이 표시한다") {
            runTest(mainDispatcher) {
                val resultList =
                    listOf(
                        Result.success(emptyList<Holiday>()),
                        Result.failure(IllegalStateException(Arb.string().next())),
                    )

                resultList.forEach { result ->
                    val viewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase(2026 to result))

                    viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))

                    viewModel.holidayListFlow().test {
                        awaitItem() shouldBe emptyList()
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        // 숨김으로 고른 공휴일을 빼는 일은 CalendarHome과 함께 쓰는 달력 공휴일 조회가 맡으므로, 그 조회가 내준 목록만 표시하고 숨김이 바뀌면 따라가는지를 본다.
        test("TC-6634424a-9197-4734-9b5e-20f24b07aa77 달력 공휴일 조회가 숨김을 뺀 목록만 표시한다") {
            runTest(mainDispatcher) {
                val hiddenHoliday = holiday(name = CONSTITUTION_DAY_NAME, date = LocalDate(year = 2026, month = Month.JULY, day = 17))
                val shownHoliday = holiday(name = LIBERATION_DAY_NAME, date = LocalDate(year = 2026, month = Month.AUGUST, day = 15))
                val visibleHolidayFlow = MutableStateFlow(listOf(hiddenHoliday, shownHoliday))
                val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getCalendarHolidayUseCase(parameter = 2026) } returns visibleHolidayFlow.map { holidayList -> Result.success(holidayList) }
                val viewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase)

                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))

                viewModel.holidayListFlow().test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(hiddenHoliday, shownHoliday)

                    visibleHolidayFlow.value = listOf(shownHoliday)
                    awaitItem() shouldBe listOf(shownHoliday)

                    viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.AUGUST))
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        // 적용 국가를 정하는 일도 달력 공휴일 조회가 맡으므로, 국가 설정마다 그 조회가 내준 공휴일을 그대로 표시하는지를 본다.
        test("TC-fa3734b4-69f4-47e4-bfac-6477570864c4 달력 공휴일 조회가 적용 국가로 내준 공휴일을 그대로 표시한다") {
            runTest(mainDispatcher) {
                val koreaHoliday = holiday(name = CONSTITUTION_DAY_NAME, date = LocalDate(year = 2026, month = Month.JULY, day = 17))
                val unitedStatesHoliday = holiday(name = INDEPENDENCE_DAY_NAME, date = LocalDate(year = 2026, month = Month.JULY, day = 4))
                val holidayListByCountry =
                    listOf(
                        listOf(koreaHoliday),
                        listOf(unitedStatesHoliday),
                        listOf(unitedStatesHoliday, koreaHoliday),
                    )
                val countryHolidayFlow = MutableStateFlow(holidayListByCountry.first())
                val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getCalendarHolidayUseCase(parameter = 2026) } returns countryHolidayFlow.map { holidayList -> Result.success(holidayList) }
                val viewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase)

                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))

                viewModel.holidayListFlow().test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe holidayListByCountry.first()

                    holidayListByCountry.drop(1).forEach { holidayList ->
                        countryHolidayFlow.value = holidayList

                        awaitItem() shouldBe holidayList
                    }
                }
            }
        }

        test("실패로 동기화가 끝난 뒤 같은 연도를 다시 요청하면 다시 동기화한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = 2026) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))
                advanceUntilIdle()
                viewModel.fetch(yearMonth = YearMonth(year = 2026, month = Month.JULY))
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(parameter = 2026) }
            }
        }

        test("달을 지정하기 전에는 공휴일을 동기화하지도 조회하지도 않는다") {
            runTest(mainDispatcher) {
                val year = Arb.int(HOLIDAY_YEAR_RANGE).next()
                val fetchHolidayUseCase = successfulFetchHolidayUseCase()
                val getCalendarHolidayUseCase = getCalendarHolidayUseCase(year to Result.success(listOf(Arb.holiday(year = year).next())))
                val viewModel = holidayViewModel(fetchHolidayUseCase = fetchHolidayUseCase, getCalendarHolidayUseCase = getCalendarHolidayUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeHolidayUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }

                coVerify(exactly = 0) { fetchHolidayUseCase(parameter = any()) }
            }
        }
    }
}

private fun RoutineHomeHolidayViewModel.holidayListFlow(): Flow<List<Holiday>> = uiState.map { uiState -> uiState.holidayList }.distinctUntilChanged()

private fun holidayViewModel(
    fetchHolidayUseCase: FetchHolidayUseCase = successfulFetchHolidayUseCase(),
    getCalendarHolidayUseCase: GetCalendarHolidayUseCase = getCalendarHolidayUseCase(),
): RoutineHomeHolidayViewModel =
    RoutineHomeHolidayViewModel(
        fetchHolidayUseCase = fetchHolidayUseCase,
        getCalendarHolidayUseCase = getCalendarHolidayUseCase,
    )

private fun successfulFetchHolidayUseCase(): FetchHolidayUseCase =
    mockk<FetchHolidayUseCase>().also { useCase ->
        coEvery { useCase(any()) } returns Result.success(emptyList())
    }

private fun getCalendarHolidayUseCase(vararg resultByYear: Pair<Int, Result<List<Holiday>>>): GetCalendarHolidayUseCase {
    val results = resultByYear.toMap()

    return mockk<GetCalendarHolidayUseCase>().also { useCase ->
        every { useCase(parameter = any()) } answers {
            flowOf(results[firstArg<Int>()] ?: Result.success(emptyList()))
        }
    }
}

private fun holiday(
    name: String,
    date: LocalDate,
): Holiday =
    Holiday(
        name = name,
        isHoliday = true,
        dateRange = date..date,
    )

private val HOLIDAY_YEAR_RANGE = 2_000..2_999
private const val CONSTITUTION_DAY_NAME = "제헌절"
private const val LIBERATION_DAY_NAME = "광복절"
private const val TEMPORARY_HOLIDAY_NAME = "임시공휴일"
private const val INDEPENDENCE_DAY_NAME = "Independence Day"
