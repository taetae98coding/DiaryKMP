package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.releaseRoutineListDetailToday
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.ui.setRoutineListDetailToday
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 필터 선택과 달력에 표시할 기록은 저장소에서 오는 값이므로 그 흐름만 테스트가 바꾼다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeFilterHolidayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(TODAY)
    }

    @After
    fun tearDown() {
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-2620cf49-f221-4418-a847-990202fe3899 태그 필터는 공휴일 표시를 바꾸지 않는다`() {
        val tagId = Arb.kotlinUuid().next()
        val holidayUiState = RoutineHomeHolidayUiState(holidayList = listOf(Holiday(name = HOLIDAY_NAME, isHoliday = true, dateRange = LocalDateRange(start = HOLIDAY_DATE, endInclusive = HOLIDAY_DATE))))
        val record =
            CalendarRoutineRecord(
                routineId = Arb.kotlinUuid().next(),
                title = ROUTINE_TITLE,
                color = Arb.long().next(),
                date = RECORD_DATE,
                amount = RECORD_AMOUNT,
                unit = "ml",
            )
        val filterUiStateFlow = MutableStateFlow(RoutineHomeScaffoldFilterUiState())
        val calendarUiStateFlow = MutableStateFlow(RoutineHomeCalendarUiState(recordList = listOf(record)))
        val holidayUiStateFlow = MutableStateFlow(holidayUiState)
        val viewModel = mockk<RoutineHomeViewModel>(relaxed = true)
        every { viewModel.routinePagingData } returns MutableStateFlow(filterCaseRoutinePagingDataOf(listOf(filterCaseRoutineSummaryOf(title = ROUTINE_TITLE))))
        every { viewModel.filterUiState } returns filterUiStateFlow
        every { viewModel.effect } returns emptyFlow()
        every { viewModel.recordEffect } returns emptyFlow()
        val calendarViewModel = mockk<RoutineHomeCalendarViewModel>(relaxed = true)
        every { calendarViewModel.uiState } returns calendarUiStateFlow
        val holidayViewModel = mockk<RoutineHomeHolidayViewModel>(relaxed = true)
        every { holidayViewModel.uiState } returns holidayUiStateFlow
        val syncViewModel = routineSyncRefreshViewModel()
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    navigateToFinishedList = {},
                    componentVisibleProvider = { RoutineHomeScaffoldComponentVisible() },
                    homeReselectEvent = emptyFlow(),
                    routineViewModel = viewModel,
                    calendarViewModel = calendarViewModel,
                    holidayViewModel = holidayViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodes(recordMatcher).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onAllNodesWithText(HOLIDAY_NAME).fetchSemanticsNodes().isNotEmpty() shouldBe true

        // 태그 A를 고르면 어느 태그와도 연결되지 않은 루틴의 기록이 조회 결과에서 빠진다.
        composeRule.runOnIdle {
            filterUiStateFlow.value = RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId))
            calendarUiStateFlow.value = RoutineHomeCalendarUiState()
        }
        composeRule.waitForIdle()

        composeRule.onAllNodes(recordMatcher).fetchSemanticsNodes().shouldBeEmpty()
        composeRule.onAllNodesWithText(HOLIDAY_NAME).fetchSemanticsNodes().isNotEmpty() shouldBe true
        // 날짜 강조는 공휴일 조회 결과로만 정해지고, 필터가 바뀌어도 그 결과는 그대로다.
        holidayUiStateFlow.value shouldBe holidayUiState
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val HOLIDAY_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 17)
        val RECORD_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 10)
        const val HOLIDAY_NAME = "제헌절"
        const val ROUTINE_TITLE = "Water"
        const val RECORD_AMOUNT = 500L
        const val DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION = "Show calendar"
        val recordMatcher = hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG) and hasContentDescription("$ROUTINE_TITLE ${RECORD_AMOUNT}ml, routine")
    }
}
