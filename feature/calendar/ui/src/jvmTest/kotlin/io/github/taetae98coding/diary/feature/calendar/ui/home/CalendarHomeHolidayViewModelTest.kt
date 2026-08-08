@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class CalendarHomeHolidayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CALENDAR-HOME-DATA-001 일반 월은 표시 중인 달의 연도 공휴일을 동기화한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val useCase = successfulFetchHolidayUseCase()
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = year) }
            }
        }

        test("TC-CALENDAR-HOME-DATA-003 1월과 2월은 이전 연도 공휴일도 동기화한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()

                listOf(Month.JANUARY, Month.FEBRUARY).forEach { month ->
                    val useCase = successfulFetchHolidayUseCase()
                    val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                    viewModel.fetch(YearMonth(year = year, month = month))
                    advanceUntilIdle()

                    coVerify(exactly = 1) { useCase(parameter = year - 1) }
                    coVerify(exactly = 1) { useCase(parameter = year) }
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-004 11월과 12월은 다음 연도 공휴일도 동기화한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()

                listOf(Month.NOVEMBER, Month.DECEMBER).forEach { month ->
                    val useCase = successfulFetchHolidayUseCase()
                    val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                    viewModel.fetch(YearMonth(year = year, month = month))
                    advanceUntilIdle()

                    coVerify(exactly = 1) { useCase(parameter = year) }
                    coVerify(exactly = 1) { useCase(parameter = year + 1) }
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-005 한 연도의 동기화가 실패해도 다른 대상 연도의 동기화를 계속한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = year) } returns
                    Result.failure(IllegalStateException("$year holiday sync failed"))
                coEvery { useCase(parameter = year + 1) } returns Result.success(emptyList())
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                viewModel.fetch(YearMonth(year = year, month = Month.DECEMBER))
                advanceUntilIdle()

                coVerifyOrder {
                    useCase(parameter = year)
                    useCase(parameter = year + 1)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-006 TC-SYNC-REFRESH-DOMAIN-008 동기화 진행 중 발생한 새 동기화 조건은 무시한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val completion = CompletableDeferred<Result<List<Holiday>>>()
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = year) } coAnswers { completion.await() }
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))
                viewModel.fetch(YearMonth(year = year + 1, month = Month.JANUARY))
                completion.complete(Result.success(emptyList()))
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = year) }
                coVerify(exactly = 0) { useCase(parameter = year + 1) }
            }
        }

        test("동기화가 실행되는 동안 진행 중임을 알린다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val completion = CompletableDeferred<Result<List<Holiday>>>()
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = year) } coAnswers { completion.await() }
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)
                viewModel.isFetching.value shouldBe false

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))
                advanceUntilIdle()
                viewModel.isFetching.value shouldBe true

                completion.complete(Result.success(emptyList()))
                advanceUntilIdle()

                viewModel.isFetching.value shouldBe false
            }
        }

        test("TC-CALENDAR-HOME-DATA-007 실패로 동기화가 끝난 뒤 발생한 새 동기화 조건은 처리한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = year) } returns
                    Result.failure(IllegalStateException("$year holiday sync failed"))
                coEvery { useCase(parameter = year + 1) } returns Result.success(emptyList())
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))
                advanceUntilIdle()
                viewModel.fetch(YearMonth(year = year + 1, month = Month.MARCH))
                advanceUntilIdle()

                coVerifyOrder {
                    useCase(parameter = year)
                    useCase(parameter = year + 1)
                }
            }
        }

        test("동기화가 취소된 뒤 발생한 새 동기화 조건은 처리한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val useCase = mockk<FetchHolidayUseCase>()
                coEvery { useCase(parameter = year) } throws CancellationException()
                coEvery { useCase(parameter = year + 1) } returns Result.success(emptyList())
                val viewModel = holidayViewModel(fetchHolidayUseCase = useCase)

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))
                advanceUntilIdle()
                viewModel.fetch(YearMonth(year = year + 1, month = Month.MARCH))
                advanceUntilIdle()

                coVerifyOrder {
                    useCase(parameter = year)
                    useCase(parameter = year + 1)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-008 화면이 표시되면 표시 대상 연도에 저장된 공휴일을 캘린더에 표시한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val oneDayHoliday = holiday(year = year)
                val multipleDayHoliday = holiday(year = year, dayCount = 3)
                val viewModel =
                    holidayViewModel(
                        getCalendarHolidayUseCase =
                            getCalendarHolidayUseCase(
                                year to Result.success(listOf(oneDayHoliday, multipleDayHoliday)),
                            ),
                    )

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(oneDayHoliday, multipleDayHoliday)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-009 표시 대상 연도의 공휴일만 캘린더에 표시한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val previousYearHoliday = holiday(year = year - 1)
                val holiday = holiday(year = year)
                val nextYearHoliday = holiday(year = year + 1)
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
                                    year - 1 to Result.success(listOf(previousYearHoliday)),
                                    year to Result.success(listOf(holiday)),
                                    year + 1 to Result.success(listOf(nextYearHoliday)),
                                ),
                        )

                    viewModel.fetch(YearMonth(year = year, month = month))

                    viewModel.holidayList.test {
                        awaitItem() shouldBe emptyList()
                        awaitItem() shouldBe expectedHolidayList
                    }
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-010 공휴일 여부가 거짓인 날도 표시 대상에 포함한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val holiday = holiday(year = year)
                val anniversary = holiday(year = year, isHoliday = false)
                val viewModel =
                    holidayViewModel(
                        getCalendarHolidayUseCase =
                            getCalendarHolidayUseCase(year to Result.success(listOf(anniversary, holiday))),
                    )

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(anniversary, holiday)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-011 표시 중인 달이 다른 연도로 바뀌면 이동한 달의 공휴일을 표시한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val otherYear = year + 2
                val holiday = holiday(year = year)
                val otherYearHoliday = holiday(year = otherYear)
                val viewModel =
                    holidayViewModel(
                        getCalendarHolidayUseCase =
                            getCalendarHolidayUseCase(
                                year to Result.success(listOf(holiday)),
                                otherYear to Result.success(listOf(otherYearHoliday)),
                            ),
                    )

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(holiday)

                    viewModel.fetch(YearMonth(year = otherYear, month = Month.JULY))

                    awaitItem() shouldBe listOf(otherYearHoliday)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-012 동기화가 진행 중이어도 이동한 달의 공휴일을 표시한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val otherYear = year + 2
                val holiday = holiday(year = year)
                val otherYearHoliday = holiday(year = otherYear)
                val completion = CompletableDeferred<Result<List<Holiday>>>()
                val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchHolidayUseCase(parameter = year) } coAnswers { completion.await() }
                val viewModel =
                    holidayViewModel(
                        fetchHolidayUseCase = fetchHolidayUseCase,
                        getCalendarHolidayUseCase =
                            getCalendarHolidayUseCase(
                                year to Result.success(listOf(holiday)),
                                otherYear to Result.success(listOf(otherYearHoliday)),
                            ),
                    )

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(holiday)

                    viewModel.fetch(YearMonth(year = otherYear, month = Month.JULY))

                    awaitItem() shouldBe listOf(otherYearHoliday)
                }

                coVerify(exactly = 0) { fetchHolidayUseCase(parameter = otherYear) }

                completion.complete(Result.success(emptyList()))
                advanceUntilIdle()
            }
        }

        test("TC-CALENDAR-HOME-DATA-013 같은 연도 안에서 달을 이동하면 표시하던 공휴일을 유지한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val holiday = holiday(year = year)
                val viewModel =
                    holidayViewModel(
                        getCalendarHolidayUseCase = getCalendarHolidayUseCase(year to Result.success(listOf(holiday))),
                    )

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(holiday)

                    viewModel.fetch(YearMonth(year = year, month = Month.AUGUST))
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.holidayList.value shouldBe listOf(holiday)
            }
        }

        test("TC-CALENDAR-HOME-DATA-014 대상 연도의 공휴일이 바뀌면 바뀐 공휴일을 표시한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val holiday = holiday(year = year)
                val changedHoliday = holiday(year = year)
                val holidayFlow = MutableStateFlow(listOf(holiday))
                val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getCalendarHolidayUseCase(parameter = year) } returns
                    holidayFlow.map { holidayList -> Result.success(holidayList) }
                val viewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase)

                viewModel.fetch(YearMonth(year = year, month = Month.JULY))

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(holiday)

                    holidayFlow.value = listOf(changedHoliday)

                    awaitItem() shouldBe listOf(changedHoliday)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-015 대상 연도에 공휴일이 없거나 조회에 실패하면 공휴일 없이 표시한다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val results =
                    listOf(
                        Result.success(emptyList<Holiday>()),
                        Result.failure(IllegalStateException("$year holiday get failed")),
                    )

                results.forEach { result ->
                    val viewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase(year to result))

                    viewModel.fetch(YearMonth(year = year, month = Month.JULY))

                    viewModel.holidayList.test {
                        awaitItem() shouldBe emptyList()
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-016 한 연도의 조회 실패는 다른 대상 연도의 공휴일 표시를 막지 않는다") {
            runTest(mainDispatcher) {
                val year = fixtureMonkey.giveMeOne<Int>().toPositiveYear()
                val holiday = holiday(year = year)
                val viewModel =
                    holidayViewModel(
                        getCalendarHolidayUseCase =
                            getCalendarHolidayUseCase(
                                year - 1 to Result.failure(IllegalStateException("${year - 1} holiday get failed")),
                                year to Result.success(listOf(holiday)),
                            ),
                    )

                viewModel.fetch(YearMonth(year = year, month = Month.JANUARY))

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(holiday)
                }
            }
        }
    }
}

private fun holidayViewModel(
    fetchHolidayUseCase: FetchHolidayUseCase = successfulFetchHolidayUseCase(),
    getCalendarHolidayUseCase: GetCalendarHolidayUseCase = getCalendarHolidayUseCase(),
): CalendarHomeHolidayViewModel =
    CalendarHomeHolidayViewModel(
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
    year: Int,
    dayCount: Int = 1,
    isHoliday: Boolean = true,
): Holiday {
    val start = randomDate(year = year)

    return Holiday(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = isHoliday,
        dateRange = start..start.plus(dayCount - 1, DateTimeUnit.DAY),
    )
}

private fun randomDate(year: Int): LocalDate =
    LocalDate(
        year = year,
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )

private fun Int.toPositiveYear(): Int = 2_000 + (toUInt() % 1_000u).toInt()
