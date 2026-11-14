@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.testing.routine.calendarRoutineRecord
import io.github.taetae98coding.diary.domain.routine.usecase.GetCalendarRoutineRecordUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

class RoutineHomeCalendarViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-be5be321-fffd-42b4-9da5-ab07615d259b 표시 중인 달이 바뀌면 이동한 달 기준의 조회 기간으로 루틴 기록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val julyRecordList = listOf(Arb.calendarRoutineRecord().next())
                val augustRecordList = listOf(Arb.calendarRoutineRecord().next(), Arb.calendarRoutineRecord().next())
                val useCase =
                    getCalendarRoutineRecordUseCase(
                        JULY_FETCH_RANGE to Result.success(julyRecordList),
                        AUGUST_FETCH_RANGE to Result.success(augustRecordList),
                    )
                val viewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeCalendarUiState()
                    awaitItem() shouldBe RoutineHomeCalendarUiState(recordList = julyRecordList)

                    viewModel.fetch(YearMonth(year = 2026, month = Month.AUGUST))

                    awaitItem() shouldBe RoutineHomeCalendarUiState(recordList = augustRecordList)
                }

                verify(exactly = 1) { useCase(parameter = JULY_FETCH_RANGE) }
                verify(exactly = 1) { useCase(parameter = AUGUST_FETCH_RANGE) }
            }
        }

        test("TC-33bb3eab-49e9-4407-94ef-de35b458b516 다른 달로 이동하면 이동한 달 기준으로 조회한 기록을 표시한다") {
            runTest(mainDispatcher) {
                val julyRecord = Arb.calendarRoutineRecord().next().copy(date = LocalDate(year = 2026, month = Month.JULY, day = 10))
                val septemberRecord = julyRecord.copy(date = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 10))
                val useCase =
                    getCalendarRoutineRecordUseCase(
                        JULY_FETCH_RANGE to Result.success(listOf(julyRecord, septemberRecord)),
                        SEPTEMBER_FETCH_RANGE to Result.success(listOf(julyRecord, septemberRecord)),
                    )
                val viewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))
                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeCalendarUiState()
                    awaitItem() shouldBe RoutineHomeCalendarUiState(recordList = listOf(julyRecord, septemberRecord))

                    viewModel.fetch(YearMonth(year = 2026, month = Month.SEPTEMBER))
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                verify(exactly = 1) { useCase(parameter = SEPTEMBER_FETCH_RANGE) }
                viewModel.uiState.value shouldBe RoutineHomeCalendarUiState(recordList = listOf(julyRecord, septemberRecord))
            }
        }

        test("TC-2e137351-7d14-401d-87b3-aeaae08bb0f5 조회한 기록이 준비되거나 바뀌면 다시 요청하지 않아도 바뀐 기록을 표시한다") {
            runTest(mainDispatcher) {
                val record =
                    Arb.calendarRoutineRecord().next().copy(
                        date = LocalDate(year = 2026, month = Month.JULY, day = 10),
                        amount = 1000,
                        unit = "ml",
                    )
                val changedRecordListList =
                    listOf(
                        listOf(record.copy(amount = 1500)),
                        listOf(record, record.copy(date = LocalDate(year = 2026, month = Month.JULY, day = 12), amount = 250)),
                        emptyList(),
                        listOf(record.copy(title = Arb.string().next())),
                        listOf(record.copy(unit = "회")),
                        listOf(record.copy(unit = "")),
                        listOf(record, record.copy(date = LocalDate(year = 2026, month = Month.JULY, day = 20))),
                    )
                val recordListFlow = MutableStateFlow(listOf(record))
                val useCase = mockk<GetCalendarRoutineRecordUseCase>()
                every { useCase(parameter = JULY_FETCH_RANGE) } returns recordListFlow.map { recordList -> Result.success(recordList) }
                val viewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeCalendarUiState()
                    awaitItem() shouldBe RoutineHomeCalendarUiState(recordList = listOf(record))

                    changedRecordListList.forEach { changedRecordList ->
                        recordListFlow.value = changedRecordList

                        awaitItem() shouldBe RoutineHomeCalendarUiState(recordList = changedRecordList)
                    }
                }

                verify(exactly = 1) { useCase(parameter = any()) }
            }
        }

        test("TC-d0483b27-35dc-49ae-a8e1-2fd510e42118 루틴 기록 조회가 실패하면 기록 없이 표시하고 달을 옮겨도 그대로다") {
            runTest(mainDispatcher) {
                val useCase = mockk<GetCalendarRoutineRecordUseCase>()
                every { useCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeCalendarUiState()
                    advanceUntilIdle()
                    expectNoEvents()

                    viewModel.fetch(YearMonth(year = 2026, month = Month.AUGUST))
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = AUGUST_FETCH_RANGE) }
            }
        }

        test("같은 달을 다시 지정해도 루틴 기록을 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val useCase = getCalendarRoutineRecordUseCase(JULY_FETCH_RANGE to Result.success(listOf(Arb.calendarRoutineRecord().next())))
                val viewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeCalendarUiState()
                    viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))
                    awaitItem()
                    viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = any()) }
            }
        }

        test("달을 지정하기 전에는 루틴 기록을 조회하지 않는다") {
            runTest(mainDispatcher) {
                val useCase = getCalendarRoutineRecordUseCase()
                val viewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineHomeCalendarUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 0) { useCase(parameter = any()) }
            }
        }
    }

    private fun getCalendarRoutineRecordUseCase(vararg resultByRange: Pair<LocalDateRange, Result<List<CalendarRoutineRecord>>>): GetCalendarRoutineRecordUseCase =
        mockk<GetCalendarRoutineRecordUseCase>().also { useCase ->
            every { useCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
            resultByRange.forEach { (dateRange, result) ->
                every { useCase(parameter = dateRange) } returns flowOf(result)
            }
        }

    private companion object {
        val JULY_FETCH_RANGE: LocalDateRange =
            LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30)
        val AUGUST_FETCH_RANGE: LocalDateRange =
            LocalDate(year = 2026, month = Month.JUNE, day = 1)..LocalDate(year = 2026, month = Month.OCTOBER, day = 31)
        val SEPTEMBER_FETCH_RANGE: LocalDateRange =
            LocalDate(year = 2026, month = Month.JULY, day = 1)..LocalDate(year = 2026, month = Month.NOVEMBER, day = 30)
    }
}
