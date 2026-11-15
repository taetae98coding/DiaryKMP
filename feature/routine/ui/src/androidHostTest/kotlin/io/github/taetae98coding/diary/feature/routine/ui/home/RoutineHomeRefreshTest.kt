package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeHolidayViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class RoutineHomeRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-0d8da035-0560-42a7-aeeb-0b800f21fc86 루틴이 표시된 목록 보기를 당기면 서버 동기화를 한 번 요청한다`() {
        val environment = setRoutineHomeScreen()

        pull()

        verify(exactly = 1) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-0d8da035-0560-42a7-aeeb-0b800f21fc86 태그 필터로 좁힌 목록 보기를 당기면 서버 동기화를 한 번 요청한다`() {
        val tagId = Arb.kotlinUuid().next()
        val environment = setRoutineHomeScreen(filterUiState = RoutineHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))

        pull()

        verify(exactly = 1) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-0d8da035-0560-42a7-aeeb-0b800f21fc86 달력 보기를 당기면 서버 동기화를 한 번 요청한다`() {
        val environment = setRoutineHomeScreen()
        showCalendar()

        pull()

        verify(exactly = 1) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-2b65aca6-9688-4d03-9528-12f85a50e2d9 목록 보기에서 새로고침이 실행되는 동안 진행이 표시되고 끝나면 사라진다`() {
        val environment = setRoutineHomeScreen()

        assertProgressWhileRefreshing(environment)
    }

    @Test
    fun `TC-2b65aca6-9688-4d03-9528-12f85a50e2d9 달력 보기에서 새로고침이 실행되는 동안 진행이 표시되고 끝나면 사라진다`() {
        val environment = setRoutineHomeScreen()
        showCalendar()

        assertProgressWhileRefreshing(environment)
    }

    // 당겨서 요청한 동기화가 실행되기 시작하고 끝나는 것은 새로고침 상태가 알린다.
    private fun assertProgressWhileRefreshing(environment: Environment) {
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()

        composeRule.runOnIdle { environment.syncUiState.value = SyncRefreshUiState(isRefreshing = true) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { environment.syncUiState.value = SyncRefreshUiState(isRefreshing = false) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun pull() {
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()
    }

    private fun showCalendar() {
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_TODAY_DESCRIPTION).assertExists()
    }

    private fun setRoutineHomeScreen(filterUiState: RoutineHomeScaffoldFilterUiState = RoutineHomeScaffoldFilterUiState()): Environment {
        val viewModel = mockk<RoutineHomeViewModel>(relaxed = true)
        every { viewModel.routinePagingData } returns MutableStateFlow(filterCaseRoutinePagingDataOf(listOf(filterCaseRoutineSummaryOf(title = ROUTINE_TITLE))))
        every { viewModel.filterUiState } returns MutableStateFlow(filterUiState)
        every { viewModel.effect } returns emptyFlow()
        every { viewModel.recordEffect } returns emptyFlow()
        val calendarViewModel = routineHomeCalendarViewModel()
        val holidayViewModel = routineHomeHolidayViewModel()
        val syncUiState = MutableStateFlow(SyncRefreshUiState())
        val syncViewModel = mockk<SyncRefreshViewModel>(relaxed = true)
        every { syncViewModel.uiState } returns syncUiState

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
        composeRule.waitForIdle()

        return Environment(syncViewModel = syncViewModel, syncUiState = syncUiState)
    }

    private class Environment(
        val syncViewModel: SyncRefreshViewModel,
        val syncUiState: MutableStateFlow<SyncRefreshUiState>,
    )

    private companion object {
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION = "Show calendar"
        const val DEFAULT_TODAY_DESCRIPTION = "Go to today"
        const val ROUTINE_TITLE = "Water"
    }
}
