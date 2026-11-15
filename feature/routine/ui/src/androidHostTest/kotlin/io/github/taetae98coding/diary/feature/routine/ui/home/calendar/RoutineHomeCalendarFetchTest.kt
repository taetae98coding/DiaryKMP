package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.domain.routine.usecase.GetCalendarRoutineRecordUseCase
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_15_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.koreanTitle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.record
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h2000dp")
class RoutineHomeCalendarFetchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val fetchedDateRangeList = mutableListOf<LocalDateRange>()
    private val augustRecord = record(title = WORKOUT_TITLE, date = LocalDate(year = 2026, month = Month.AUGUST, day = 20))
    private val calendarViewModel = spyk(RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = getCalendarRoutineRecordUseCase()))
    private val holidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = MutableStateFlow(RoutineHomeHolidayUiState()))
    private var isRoutineHomeShown by mutableStateOf(true)

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-be5be321-fffd-42b4-9da5-ab07615d259b 표시 중인 달이 바뀌면 이동한 달을 기준으로 루틴 기록을 조회한다`() {
        composeRule.setContent { RoutineHomeTestScreen() }
        composeRule.waitForIdle()
        fetchedDateRangeList shouldBe listOf(JULY_FETCH_RANGE)

        composeRule.routineHomeCalendarMoveToNextMonth()

        fetchedDateRangeList shouldBe listOf(JULY_FETCH_RANGE, AUGUST_FETCH_RANGE)
        assertAugustRecordShown()
    }

    @Test
    fun `TC-be5be321-fffd-42b4-9da5-ab07615d259b 화면이 재생성되면 그 시점의 달을 기준으로 루틴 기록을 다시 조회한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { RoutineHomeTestScreen() }
        composeRule.routineHomeCalendarMoveToNextMonth()
        verify(exactly = 1) { calendarViewModel.fetch(yearMonth = AUGUST_2026) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        verify(exactly = 2) { calendarViewModel.fetch(yearMonth = AUGUST_2026) }
        verify(exactly = 1) { calendarViewModel.fetch(yearMonth = RoutineHomeCalendarTestFixture.JULY_2026) }
        fetchedDateRangeList.last() shouldBe AUGUST_FETCH_RANGE
        assertAugustRecordShown()
    }

    @Test
    fun `TC-be5be321-fffd-42b4-9da5-ab07615d259b 루틴 추가에서 돌아오면 그 시점의 달을 기준으로 루틴 기록을 다시 조회한다`() {
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isRoutineHomeShown) {
                saveableStateHolder.SaveableStateProvider(key = ROUTINE_HOME_ENTRY_KEY) { RoutineHomeTestScreen() }
            }
        }
        composeRule.routineHomeCalendarMoveToNextMonth()
        verify(exactly = 1) { calendarViewModel.fetch(yearMonth = AUGUST_2026) }

        composeRule.runOnIdle { isRoutineHomeShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isRoutineHomeShown = true }
        composeRule.waitForIdle()

        verify(exactly = 2) { calendarViewModel.fetch(yearMonth = AUGUST_2026) }
        verify(exactly = 1) { calendarViewModel.fetch(yearMonth = RoutineHomeCalendarTestFixture.JULY_2026) }
        fetchedDateRangeList.last() shouldBe AUGUST_FETCH_RANGE
        assertAugustRecordShown()
    }

    private fun assertAugustRecordShown() {
        composeRule.onNodeWithText(koreanTitle(AUGUST_2026)).assertIsDisplayed()
        composeRule.routineHomeCalendarItemBoundsOn(text = WORKOUT_ONCE_TEXT, date = augustRecord.date, yearMonth = AUGUST_2026) shouldHaveSize 1
    }

    @Composable
    private fun RoutineHomeTestScreen() {
        RoutineHomeCalendarTestScreen(
            today = JULY_15_2026,
            calendarViewModel = calendarViewModel,
            holidayViewModel = holidayViewModel,
        )
    }

    // 8월 기준의 조회 기간으로 조회했을 때만 8월 20일의 기록을 내준다.
    private fun getCalendarRoutineRecordUseCase(): GetCalendarRoutineRecordUseCase =
        mockk<GetCalendarRoutineRecordUseCase>().also { useCase ->
            every { useCase(parameter = any()) } answers {
                val dateRange = firstArg<LocalDateRange>()
                fetchedDateRangeList += dateRange

                flowOf(Result.success(if (dateRange == AUGUST_FETCH_RANGE) listOf(augustRecord) else emptyList<CalendarRoutineRecord>()))
            }
        }

    private companion object {
        const val ROUTINE_HOME_ENTRY_KEY = "RoutineHome"
        const val WORKOUT_TITLE = "운동"
        const val WORKOUT_ONCE_TEXT = "운동 1회"
        val AUGUST_2026 = YearMonth(year = 2026, month = Month.AUGUST)
        val JULY_FETCH_RANGE: LocalDateRange = LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30)
        val AUGUST_FETCH_RANGE: LocalDateRange = LocalDate(year = 2026, month = Month.JUNE, day = 1)..LocalDate(year = 2026, month = Month.OCTOBER, day = 31)
    }
}
