package io.github.taetae98coding.diary.feature.timetable.ui

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.lunar.usecase.FetchLunarUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

class CalendarTimetableBirthdayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-a365b5ad-e7b3-4323-960b-e009d4543f98 생일 조회 기간은 메모 조회 기간과 같다") {
            val caseList =
                listOf(
                    day(day = 23)..day(day = 23) to day(day = 22)..day(day = 24),
                    day(day = 20)..day(day = 26) to day(day = 13)..LocalDate(year = 2026, month = 10, day = 3),
                )

            caseList.forEach { (dateRange, expected) ->
                runTest(mainDispatcher) {
                    val getUseCase = getBirthdayUseCase()
                    val viewModel = birthdayViewModel(getCalendarContactBirthdayUseCase = getUseCase)

                    viewModel.uiState.test {
                        awaitItem()
                        viewModel.fetch(dateRange = dateRange)
                        advanceUntilIdle()

                        verify(exactly = 1) { getUseCase(parameter = expected) }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("조회한 생일을 그대로 제공한다") {
            runTest(mainDispatcher) {
                val birthday = CalendarContactBirthday(contactId = Arb.kotlinUuid().next(), name = Arb.nonBlankString().next(), nameQualifier = "", date = day(day = 24))
                val getUseCase = mockk<GetCalendarContactBirthdayUseCase>()
                every { getUseCase(parameter = any()) } returns flowOf(Result.success(listOf(birthday)))
                val viewModel = birthdayViewModel(getCalendarContactBirthdayUseCase = getUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarTimetableBirthdayUiState()
                    viewModel.fetch(dateRange = day(day = 24)..day(day = 24))

                    awaitItem() shouldBe CalendarTimetableBirthdayUiState(birthdayList = listOf(birthday))
                }
            }
        }

        test("TC-9c8e653c-ad81-480b-99a2-dac567c94496 생일 조회에 실패하면 생일 없이 제공한다") {
            runTest(mainDispatcher) {
                val getUseCase = mockk<GetCalendarContactBirthdayUseCase>()
                every { getUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(Arb.nonBlankString().next())))
                val viewModel = birthdayViewModel(getCalendarContactBirthdayUseCase = getUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarTimetableBirthdayUiState()
                    viewModel.fetch(dateRange = day(day = 24)..day(day = 24))
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-abe1bc82-dbfd-4b51-9d47-e1ab9362cc19 조회 기간이 연도를 넘으면 두 연도의 음력 자료를 동기화한다") {
            runTest(mainDispatcher) {
                val fetchUseCase = fetchLunarUseCase()
                val viewModel = birthdayViewModel(fetchLunarUseCase = fetchUseCase)

                viewModel.fetch(dateRange = LocalDate(year = 2026, month = 12, day = 31)..LocalDate(year = 2026, month = 12, day = 31))
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchUseCase(parameter = 2026) }
                coVerify(exactly = 1) { fetchUseCase(parameter = 2027) }
            }
        }

        test("TC-fb0d7338-9eb2-47e7-903f-1433d289a6b3 같은 연도의 음력 자료 동기화가 진행 중이면 다시 요청하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<List<LunarDate>>>()
                val fetchUseCase = mockk<FetchLunarUseCase>()
                coEvery { fetchUseCase(parameter = any()) } coAnswers { completion.await() }
                val viewModel = birthdayViewModel(fetchLunarUseCase = fetchUseCase)

                viewModel.fetch(dateRange = day(day = 23)..day(day = 23))
                advanceUntilIdle()
                viewModel.fetch(dateRange = day(day = 24)..day(day = 24))
                completion.complete(Result.success(emptyList()))
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchUseCase(parameter = 2026) }
            }
        }

        test("음력 자료 동기화가 끝나면 다음 요청에서 다시 동기화한다") {
            runTest(mainDispatcher) {
                val fetchUseCase = fetchLunarUseCase()
                val viewModel = birthdayViewModel(fetchLunarUseCase = fetchUseCase)

                viewModel.fetch(dateRange = day(day = 23)..day(day = 23))
                advanceUntilIdle()
                viewModel.fetch(dateRange = day(day = 23)..day(day = 23))
                advanceUntilIdle()

                coVerify(exactly = 2) { fetchUseCase(parameter = 2026) }
            }
        }
    }

    private fun birthdayViewModel(
        fetchLunarUseCase: FetchLunarUseCase = fetchLunarUseCase(),
        getCalendarContactBirthdayUseCase: GetCalendarContactBirthdayUseCase = getBirthdayUseCase(),
    ): CalendarTimetableBirthdayViewModel =
        CalendarTimetableBirthdayViewModel(
            fetchLunarUseCase = fetchLunarUseCase,
            getCalendarContactBirthdayUseCase = getCalendarContactBirthdayUseCase,
        )

    private fun fetchLunarUseCase(): FetchLunarUseCase =
        mockk<FetchLunarUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(emptyList())
        }

    private fun getBirthdayUseCase(): GetCalendarContactBirthdayUseCase =
        mockk<GetCalendarContactBirthdayUseCase>().also { useCase ->
            every { useCase(parameter = any<LocalDateRange>()) } returns flowOf(Result.success(emptyList()))
        }

    private fun day(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)
}
