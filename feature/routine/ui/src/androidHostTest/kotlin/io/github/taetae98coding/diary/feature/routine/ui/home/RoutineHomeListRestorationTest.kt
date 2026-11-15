package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    // 필터 선택과 목록은 화면 재생성을 넘어 유지되는 ViewModel이 들고 있으므로 같은 흐름을 다시 구독한다.
    @Test
    fun `TC-bc90d6bc-3f87-4666-86da-8d9ef50a5935 화면이 재생성되어도 보던 목록 위치와 필터 선택을 유지한다`() {
        val tagId = Arb.kotlinUuid().next()
        val summaryList = List(ROUTINE_COUNT) { index -> filterCaseRoutineSummaryOf(title = "$TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}") }
        val firstTitle =
            summaryList
                .first()
                .routine.detail.title
        val scrolledTitle = summaryList[SCROLLED_INDEX].routine.detail.title
        val viewModel = mockk<RoutineHomeViewModel>(relaxed = true)
        every { viewModel.routinePagingData } returns MutableStateFlow(filterCaseRoutinePagingDataOf(summaryList))
        every { viewModel.filterUiState } returns MutableStateFlow(RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))
        every { viewModel.effect } returns emptyFlow()
        every { viewModel.recordEffect } returns emptyFlow()
        val calendarViewModel = routineHomeCalendarViewModel()
        val holidayViewModel = routineHomeHolidayViewModel()
        val syncViewModel = routineSyncRefreshViewModel()
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
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
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(firstTitle).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.filterCaseRefreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.onNodeWithText(scrolledTitle).assertIsDisplayed()
        composeRule.onNodeWithText(firstTitle).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(DEFAULT_FILTER_DESCRIPTION) and hasFilterCaseStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION)).assertExists()
        composeRule.onNodeWithText(scrolledTitle).assertIsDisplayed()
        composeRule.onNodeWithText(firstTitle).assertDoesNotExist()
    }

    private companion object {
        const val ROUTINE_COUNT = 40
        const val SCROLLED_INDEX = 30
        const val TIMEOUT_MILLIS = 5_000L
        const val TITLE_PREFIX = "RestorationRoutine"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter"
        const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Tag filter applied"
    }
}
