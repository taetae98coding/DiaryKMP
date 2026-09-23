@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home.memo

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.MoveMemoUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.toDateRange
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class CalendarHomeMemoViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CALENDAR-HOME-DATA-017 화면이 표시되면 두 달 전 1일부터 두 달 후 마지막 날까지의 메모를 조회한다") {
            runTest(mainDispatcher) {
                val expectedDateRange =
                    LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30)
                val memoList = listOf(memo(), memo())
                val useCase = getCalendarMemoUseCase(expectedDateRange to Result.success(memoList))
                val viewModel = memoViewModel(getCalendarMemoUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe memoList
                }

                verify(exactly = 1) { useCase(parameter = expectedDateRange) }
            }
        }

        test("연도 경계를 넘는 달도 두 달 전 1일부터 두 달 후 마지막 날까지 조회한다") {
            runTest(mainDispatcher) {
                val januaryDateRange = LocalDate(year = 2025, month = Month.NOVEMBER, day = 1)..LocalDate(year = 2026, month = Month.MARCH, day = 31)
                val decemberDateRange = LocalDate(year = 2026, month = Month.OCTOBER, day = 1)..LocalDate(year = 2027, month = Month.FEBRUARY, day = 28)
                val expectedRangeByYearMonth =
                    listOf(
                        YearMonth(year = 2026, month = Month.JANUARY) to januaryDateRange,
                        YearMonth(year = 2026, month = Month.DECEMBER) to decemberDateRange,
                    )

                expectedRangeByYearMonth.forEach { (yearMonth, expectedDateRange) ->
                    val useCase = getCalendarMemoUseCase(expectedDateRange to Result.success(emptyList()))
                    val viewModel = memoViewModel(getCalendarMemoUseCase = useCase)

                    viewModel.fetch(yearMonth)

                    viewModel.memoList.test {
                        awaitItem() shouldBe emptyList()
                        advanceUntilIdle()
                    }

                    verify(exactly = 1) { useCase(parameter = expectedDateRange) }
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-018 표시 중인 달이 바뀌면 이동한 달 기준의 조회 기간으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val julyDateRange =
                    LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30)
                val septemberDateRange =
                    LocalDate(year = 2026, month = Month.JULY, day = 1)..LocalDate(year = 2026, month = Month.NOVEMBER, day = 30)
                val mayMemo = memo()
                val novemberMemo = memo()
                val useCase =
                    getCalendarMemoUseCase(
                        julyDateRange to Result.success(listOf(mayMemo)),
                        septemberDateRange to Result.success(listOf(novemberMemo)),
                    )
                val viewModel = memoViewModel(getCalendarMemoUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(mayMemo)

                    viewModel.fetch(YearMonth(year = 2026, month = Month.SEPTEMBER))

                    awaitItem() shouldBe listOf(novemberMemo)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-019 메모 조회가 실패하면 메모 없이 표시한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<GetCalendarMemoUseCase>()
                every { useCase(parameter = any()) } returns
                    flowOf(Result.failure(IllegalStateException("memo get failed")))
                val viewModel = memoViewModel(getCalendarMemoUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("같은 달을 다시 지정해도 메모를 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val yearMonth = YearMonth(year = 2026, month = Month.JULY)
                val useCase = getCalendarMemoUseCase()
                val viewModel = memoViewModel(getCalendarMemoUseCase = useCase)

                viewModel.fetch(yearMonth)

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()
                    advanceUntilIdle()

                    viewModel.fetch(yearMonth)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = any()) }
            }
        }

        test("선택한 태그가 있으면 필터 적용 상태를 노출한다") {
            runTest(mainDispatcher) {
                val selectedTagList = listOf(tag())
                val viewModel =
                    memoViewModel(
                        getCalendarFilterUseCase =
                            calendarFilterUseCase(
                                flow = flowOf(Result.success(selectedTagList)),
                            ),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe CalendarHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe CalendarHomeScaffoldFilterUiState(isApplied = true)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("필터 조회에 실패하면 적용되지 않은 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    memoViewModel(
                        getCalendarFilterUseCase =
                            calendarFilterUseCase(
                                flow = flowOf(Result.failure(IllegalStateException())),
                            ),
                    )

                viewModel.filterUiState.test {
                    awaitItem() shouldBe CalendarHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("move는 MoveMemoUseCase에 이동 파라미터를 그대로 전달한다") {
            runTest(mainDispatcher) {
                val moveMemoUseCase = mockk<MoveMemoUseCase>()
                coEvery { moveMemoUseCase(parameter = any()) } returns Result.success(Unit)
                val viewModel = memoViewModel(moveMemoUseCase = moveMemoUseCase)
                val id = Uuid.random()
                val dateTime =
                    MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = Month.JULY, day = 14)..LocalDate(year = 2026, month = Month.JULY, day = 16))
                val dateRange =
                    LocalDate(year = 2026, month = Month.JULY, day = 21)..LocalDate(year = 2026, month = Month.JULY, day = 23)

                viewModel.move(id = id, fromDateTime = dateTime, toDateRange = dateRange)
                advanceUntilIdle()

                coVerify(exactly = 1) {
                    moveMemoUseCase(parameter = MoveMemoUseCase.Parameter(id = id, fromDateTime = dateTime, toDateRange = dateRange))
                }
            }
        }

        test("저장된 메모가 바뀌면 조작 없이 표시할 메모가 갱신된다") {
            runTest(mainDispatcher) {
                val memo = memo()
                val changedMemo = memo()
                val memoFlow = MutableStateFlow(listOf(memo))
                val useCase = mockk<GetCalendarMemoUseCase>()
                every { useCase(parameter = any()) } returns
                    memoFlow.map { memoList -> Result.success(memoList) }
                val viewModel = memoViewModel(getCalendarMemoUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(memo)

                    memoFlow.value = listOf(changedMemo)

                    awaitItem() shouldBe listOf(changedMemo)
                }
            }
        }
    }
}

private fun memoViewModel(
    getCalendarFilterUseCase: GetCalendarFilterUseCase = calendarFilterUseCase(flow = emptyFlow()),
    getCalendarMemoUseCase: GetCalendarMemoUseCase = getCalendarMemoUseCase(),
    moveMemoUseCase: MoveMemoUseCase = mockk(),
): CalendarHomeMemoViewModel =
    CalendarHomeMemoViewModel(
        getCalendarFilterUseCase = getCalendarFilterUseCase,
        getCalendarMemoUseCase = getCalendarMemoUseCase,
        moveMemoUseCase = moveMemoUseCase,
    )

private fun calendarFilterUseCase(flow: Flow<Result<List<Tag>>>): GetCalendarFilterUseCase {
    val getCalendarFilterUseCase = mockk<GetCalendarFilterUseCase>()
    every { getCalendarFilterUseCase(parameter = Unit) } returns flow

    return getCalendarFilterUseCase
}

private fun tag(): Tag =
    fixtureMonkey
        .giveMeKotlinBuilder<Tag>()
        .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
        .sample()

private fun getCalendarMemoUseCase(vararg resultByDateRange: Pair<LocalDateRange, Result<List<CalendarMemo>>>): GetCalendarMemoUseCase {
    val results = resultByDateRange.toMap()

    return mockk<GetCalendarMemoUseCase>().also { useCase ->
        every { useCase(parameter = any()) } answers {
            flowOf(results[firstArg<LocalDateRange>()] ?: Result.success(emptyList()))
        }
    }
}

private fun memo(): CalendarMemo {
    val start =
        LocalDate(
            year = 2026,
            month = Month.JULY,
            day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
        )

    return CalendarMemo(
        id = Uuid.random(),
        title = fixtureMonkey.giveMeOne(),
        color = fixtureMonkey.giveMeOne(),
        dateTime = MemoDateTime.AllDay(dateRange = start..start),
    )
}
