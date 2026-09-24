@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home.birthday

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.lunar.usecase.FetchLunarUseCase
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
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class CalendarHomeBirthdayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CALENDAR-HOME-DATA-032 화면이 표시되면 두 달 전 1일부터 두 달 후 마지막 날까지의 생일을 조회한다") {
            runTest(mainDispatcher) {
                val expectedDateRange =
                    LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30)
                val birthdayList = listOf(birthday(), birthday())
                val useCase = getCalendarContactBirthdayUseCase(expectedDateRange to Result.success(birthdayList))
                val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase(), getCalendarContactBirthdayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.birthdayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe birthdayList
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
                    val useCase = getCalendarContactBirthdayUseCase(expectedDateRange to Result.success(emptyList()))
                    val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase(), getCalendarContactBirthdayUseCase = useCase)

                    viewModel.fetch(yearMonth)

                    viewModel.birthdayList.test {
                        awaitItem() shouldBe emptyList()
                        advanceUntilIdle()
                    }

                    verify(exactly = 1) { useCase(parameter = expectedDateRange) }
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-033 표시 중인 달이 바뀌면 이동한 달 기준의 조회 기간으로 다시 조회한다") {
            runTest(mainDispatcher) {
                val julyDateRange =
                    LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30)
                val septemberDateRange =
                    LocalDate(year = 2026, month = Month.JULY, day = 1)..LocalDate(year = 2026, month = Month.NOVEMBER, day = 30)
                val mayBirthday = birthday()
                val novemberBirthday = birthday()
                val useCase =
                    getCalendarContactBirthdayUseCase(
                        julyDateRange to Result.success(listOf(mayBirthday)),
                        septemberDateRange to Result.success(listOf(novemberBirthday)),
                    )
                val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase(), getCalendarContactBirthdayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.birthdayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(mayBirthday)

                    viewModel.fetch(YearMonth(year = 2026, month = Month.SEPTEMBER))

                    awaitItem() shouldBe listOf(novemberBirthday)
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-034 생일 조회가 실패하면 생일 없이 표시한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<GetCalendarContactBirthdayUseCase>()
                every { useCase(parameter = any()) } returns
                    flowOf(Result.failure(IllegalStateException("birthday get failed")))
                val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase(), getCalendarContactBirthdayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.birthdayList.test {
                    awaitItem() shouldBe emptyList()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("같은 달을 다시 지정해도 생일을 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val yearMonth = YearMonth(year = 2026, month = Month.JULY)
                val useCase = getCalendarContactBirthdayUseCase()
                val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase(), getCalendarContactBirthdayUseCase = useCase)

                viewModel.fetch(yearMonth)

                viewModel.birthdayList.test {
                    awaitItem() shouldBe emptyList()
                    advanceUntilIdle()

                    viewModel.fetch(yearMonth)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = any()) }
            }
        }

        test("TC-CALENDAR-HOME-DATA-038 화면이 표시되면 생일 조회 기간이 속한 연도의 음력 자료를 동기화한다") {
            val expectedYearListByYearMonth =
                listOf(
                    YearMonth(year = 2026, month = Month.JULY) to listOf(2026),
                    YearMonth(year = 2026, month = Month.DECEMBER) to listOf(2026, 2027),
                    YearMonth(year = 2026, month = Month.JANUARY) to listOf(2025, 2026),
                )

            expectedYearListByYearMonth.forEach { (yearMonth, expectedYearList) ->
                runTest(mainDispatcher) {
                    val fetchLunarUseCase = fetchLunarUseCase()
                    val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase, getCalendarContactBirthdayUseCase = getCalendarContactBirthdayUseCase())

                    viewModel.fetch(yearMonth)
                    advanceUntilIdle()

                    expectedYearListByYearMonth
                        .flatMap { (_, yearList) -> yearList }
                        .distinct()
                        .forEach { year ->
                            coVerify(exactly = if (year in expectedYearList) 1 else 0) { fetchLunarUseCase(parameter = year) }
                        }
                }
            }
        }

        test("TC-CALENDAR-HOME-DATA-039 표시 중인 달이 바뀌면 이동한 달의 조회 기간이 속한 연도의 음력 자료를 동기화한다") {
            runTest(mainDispatcher) {
                val fetchLunarUseCase = fetchLunarUseCase()
                val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase, getCalendarContactBirthdayUseCase = getCalendarContactBirthdayUseCase())

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))
                advanceUntilIdle()
                viewModel.fetch(YearMonth(year = 2026, month = Month.NOVEMBER))
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchLunarUseCase(parameter = 2026) }
                coVerify(exactly = 1) { fetchLunarUseCase(parameter = 2027) }
            }
        }

        test("TC-CALENDAR-HOME-DATA-040 음력 자료 동기화가 실패해도 생일 조회는 그대로 제공한다") {
            runTest(mainDispatcher) {
                val birthdayList = listOf(birthday())
                val fetchLunarUseCase = mockk<FetchLunarUseCase>()
                coEvery { fetchLunarUseCase(parameter = any()) } returns Result.failure(IllegalStateException("lunar fetch failed"))
                val useCase =
                    getCalendarContactBirthdayUseCase(
                        LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30) to Result.success(birthdayList),
                    )
                val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase, getCalendarContactBirthdayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.birthdayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe birthdayList
                }
            }
        }

        test("저장된 연락처가 바뀌면 조작 없이 표시할 생일이 갱신된다") {
            runTest(mainDispatcher) {
                val birthday = birthday()
                val changedBirthday = birthday()
                val birthdayFlow = MutableStateFlow(listOf(birthday))
                val useCase = mockk<GetCalendarContactBirthdayUseCase>()
                every { useCase(parameter = any()) } returns birthdayFlow.map { list -> Result.success(list) }
                val viewModel = CalendarHomeBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase(), getCalendarContactBirthdayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.birthdayList.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(birthday)

                    birthdayFlow.value = listOf(changedBirthday)

                    awaitItem() shouldBe listOf(changedBirthday)
                }
            }
        }
    }
}

private fun fetchLunarUseCase(): FetchLunarUseCase =
    mockk<FetchLunarUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(emptyList())
    }

private fun getCalendarContactBirthdayUseCase(vararg resultByDateRange: Pair<LocalDateRange, Result<List<CalendarContactBirthday>>>): GetCalendarContactBirthdayUseCase {
    val results = resultByDateRange.toMap()

    return mockk<GetCalendarContactBirthdayUseCase>().also { useCase ->
        every { useCase(parameter = any()) } answers {
            flowOf(results[firstArg<LocalDateRange>()] ?: Result.success(emptyList()))
        }
    }
}

private fun birthday(): CalendarContactBirthday =
    CalendarContactBirthday(
        contactId = Uuid.random(),
        name = fixtureMonkey.giveMeOne(),
        date =
            LocalDate(
                year = 2026,
                month = Month.JULY,
                day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
            ),
    )
