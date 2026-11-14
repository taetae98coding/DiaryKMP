package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetCalendarRoutineRecordUseCase
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScreen
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_REFRESHING_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.kotest.assertions.withClue
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

// RoutineHomeScreen은 오늘이 속한 달에서 달력을 시작하므로, 이 화면의 요청은 기기의 오늘이 속한 달과 그 다음 달을 기준으로 확인한다.
// 달마다 요청하는 연도는 RoutineHomeHolidayViewModelTest가 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeHolidayFetchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val currentYearMonth: YearMonth = Clock.System.todayIn(TimeZone.currentSystemDefault()).yearMonth
    private val nextYearMonth: YearMonth = currentYearMonth.plus(1, DateTimeUnit.MONTH)
    private val screen = ReturnableScreen()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-3f5d4d91-4ba0-4b35-9151-e1acbb05c55c 달력 보기로 바꾸면 표시 중인 달의 공휴일 동기화를 요청한다`() {
        val holidayViewModel = holidayViewModel()
        setRoutineHomeScreen(holidayViewModel = holidayViewModel)
        verify(exactly = 0) { holidayViewModel.fetch(yearMonth = any()) }

        showCalendar()

        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = currentYearMonth) }
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = any()) }
    }

    @Test
    fun `TC-badc63d3-8d62-4623-b3d2-6ae72d3c1398 표시 중인 달이 바뀌면 이동한 달의 공휴일 동기화를 요청한다`() {
        val holidayViewModel = holidayViewModel()
        setRoutineHomeScreen(holidayViewModel = holidayViewModel)
        showCalendar()

        composeRule.routineHomeCalendarMoveToNextMonth()

        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = currentYearMonth) }
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = nextYearMonth) }
    }

    @Test
    fun `TC-f49668d9-0cda-41eb-bd93-92322a3dc8fd 목록 보기에 있는 동안에는 진입해도 새로고침해도 공휴일을 동기화하지 않는다`() {
        val holidayViewModel = holidayViewModel()
        val syncViewModel = routineSyncRefreshViewModel()
        setRoutineHomeScreen(holidayViewModel = holidayViewModel, syncViewModel = syncViewModel)
        verify(exactly = 0) { holidayViewModel.fetch(yearMonth = any()) }

        pullToRefresh()

        verify(exactly = 1) { syncViewModel.refresh() }
        verify(exactly = 0) { holidayViewModel.fetch(yearMonth = any()) }
    }

    @Test
    fun `TC-384da1ca-f392-41ea-832b-60e069109671 달력 보기에서 당겨 새로고침하면 표시 중인 달의 공휴일도 다시 동기화한다`() {
        val holidayViewModel = holidayViewModel()
        val syncViewModel = routineSyncRefreshViewModel()
        setRoutineHomeScreen(holidayViewModel = holidayViewModel, syncViewModel = syncViewModel)
        showCalendar()
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = currentYearMonth) }
        verify(exactly = 0) { syncViewModel.refresh() }

        pullToRefresh()

        verify(exactly = 1) { syncViewModel.refresh() }
        verify(exactly = 2) { holidayViewModel.fetch(yearMonth = currentYearMonth) }
    }

    @Test
    fun `TC-48b863d4-42c4-40e5-9b06-d1241d6f47f6 진입과 보기 전환, 달 이동만으로는 계정 데이터의 서버 동기화를 요청하지 않는다`() {
        val syncViewModel = routineSyncRefreshViewModel()
        setRoutineHomeScreen(syncViewModel = syncViewModel)

        showCalendar()
        composeRule.routineHomeCalendarMoveToNextMonth()

        verify(exactly = 0) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-93383832-1ad1-4393-b75c-991946abc665 달력 보기를 둔 채 다른 화면에서 돌아오면 그 시점의 달을 기준으로 공휴일을 다시 동기화한다`() {
        val holidayViewModel = holidayViewModel()
        setRoutineHomeScreen(holidayViewModel = holidayViewModel)
        showCalendar()
        composeRule.routineHomeCalendarMoveToNextMonth()
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = nextYearMonth) }

        listOf("루틴 추가", "루틴 상세").forEachIndexed { index, destination ->
            composeRule.runOnIdle { screen.isShown = false }
            composeRule.waitForIdle()
            composeRule.runOnIdle { screen.isShown = true }
            composeRule.waitForIdle()

            withClue(destination) {
                verify(exactly = index + 2) { holidayViewModel.fetch(yearMonth = nextYearMonth) }
                verify(exactly = 1) { holidayViewModel.fetch(yearMonth = currentYearMonth) }
            }
        }
    }

    @Test
    fun `TC-93383832-1ad1-4393-b75c-991946abc665 다른 앱에 다녀오는 것만으로는 공휴일을 다시 동기화하지 않는다`() {
        val holidayViewModel = holidayViewModel()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setRoutineHomeScreen(holidayViewModel = holidayViewModel, lifecycleOwner = lifecycleOwner)
        showCalendar()
        composeRule.routineHomeCalendarMoveToNextMonth()
        verify(exactly = 2) { holidayViewModel.fetch(yearMonth = any()) }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        verify(exactly = 2) { holidayViewModel.fetch(yearMonth = any()) }
    }

    // 이 화면이 동기화를 요청할 수 있는 길은 화면이 받는 ViewModel뿐이므로, 그 ViewModel들이 부르는 요청이 공휴일과 루틴 기록, 계정 데이터 새로고침뿐인지 본다.
    @Test
    fun `TC-8ee42f6c-a618-40e3-8a77-06268a932be2 달력 보기로 바꾸고 달을 옮긴 뒤 새로고침해도 공휴일 말고 다른 연도별 자료를 동기화하지 않는다`() {
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = any()) } returns Result.success(emptyList())
        val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
        every { getCalendarHolidayUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
        val getCalendarRoutineRecordUseCase = mockk<GetCalendarRoutineRecordUseCase>()
        every { getCalendarRoutineRecordUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
        val syncViewModel = routineSyncRefreshViewModel()
        setRoutineHomeScreen(
            calendarViewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = getCalendarRoutineRecordUseCase),
            holidayViewModel = RoutineHomeHolidayViewModel(fetchHolidayUseCase = fetchHolidayUseCase, getCalendarHolidayUseCase = getCalendarHolidayUseCase),
            syncViewModel = syncViewModel,
        )

        showCalendar()
        composeRule.routineHomeCalendarMoveToNextMonth()
        pullToRefresh()

        verify(exactly = 1) { syncViewModel.refresh() }
        coVerify(atLeast = 1) { fetchHolidayUseCase(parameter = any()) }
        verify(atLeast = 1) { getCalendarHolidayUseCase(parameter = any()) }
        verify(atLeast = 1) { getCalendarRoutineRecordUseCase(parameter = any()) }
        confirmVerified(fetchHolidayUseCase, getCalendarHolidayUseCase, getCalendarRoutineRecordUseCase)
    }

    @Test
    fun `TC-2b48371b-6d28-4d10-865d-ebcb77162a89 로그인하지 않은 상태에서도 달력 보기에서 당기면 공휴일을 다시 가져오고 그동안 진행을 표시한다`() {
        val holidayUiState = MutableStateFlow(RoutineHomeHolidayUiState())
        val holidayViewModel = holidayViewModel(uiState = holidayUiState)
        // 화면은 계정 상태를 모른 채 새로고침만 요청한다. 로그인하지 않은 상태의 그 요청이 서버 동기화를 시작하지 않는 것은 RequestSyncUseCaseTest가 같은 ID로 확인한다.
        val guestSyncViewModel = routineSyncRefreshViewModel()
        setRoutineHomeScreen(holidayViewModel = holidayViewModel, syncViewModel = guestSyncViewModel)
        showCalendar()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
        clearMocks(holidayViewModel, answers = false)

        pullToRefresh()
        verify(exactly = 1) { guestSyncViewModel.refresh() }
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = currentYearMonth) }

        holidayUiState.update { uiState -> uiState.copy(isFetching = true) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        holidayUiState.update { uiState -> uiState.copy(isFetching = false) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-d5c9a29d-b5d5-432a-88c9-4a97ea7c1e53 달력 보기가 표시되거나 달이 바뀌어 시작된 공휴일 동기화도 진행을 표시한다`() {
        val holidayUiState = MutableStateFlow(RoutineHomeHolidayUiState())
        val holidayViewModel = holidayViewModel(uiState = holidayUiState)
        // 요청을 받으면 동기화가 실행 중인 상태가 된다.
        every { holidayViewModel.fetch(yearMonth = any()) } answers { holidayUiState.update { uiState -> uiState.copy(isFetching = true) } }
        setRoutineHomeScreen(holidayViewModel = holidayViewModel)
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()

        showCalendar()

        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = currentYearMonth) }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { holidayUiState.update { uiState -> uiState.copy(isFetching = false) } }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()

        composeRule.routineHomeCalendarMoveToNextMonth()

        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = nextYearMonth) }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    private fun showCalendar() {
        composeRule.routineHomeCalendarToggleViewMode(description = DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION)
    }

    private fun pullToRefresh() {
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()
    }

    private fun holidayViewModel(uiState: MutableStateFlow<RoutineHomeHolidayUiState> = MutableStateFlow(RoutineHomeHolidayUiState())): RoutineHomeHolidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = uiState)

    /** 다른 화면으로 이동하면 RoutineHome이 구성에서 빠지고 저장 상태만 남으며, 돌아오면 그 저장 상태로 다시 구성된다. */
    private class ReturnableScreen {
        var isShown by mutableStateOf(true)
    }

    private fun setRoutineHomeScreen(
        calendarViewModel: RoutineHomeCalendarViewModel = RoutineHomeCalendarTestFixture.calendarViewModel(uiState = MutableStateFlow(RoutineHomeCalendarUiState())),
        holidayViewModel: RoutineHomeHolidayViewModel = holidayViewModel(),
        syncViewModel: SyncRefreshViewModel = routineSyncRefreshViewModel(),
        lifecycleOwner: TestLifecycleOwner? = null,
    ) {
        val routineViewModel = routineHomeViewModel()

        composeRule.setContent {
            val saveableStateHolder = rememberSaveableStateHolder()

            CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                DiaryTheme {
                    if (screen.isShown) {
                        saveableStateHolder.SaveableStateProvider(key = ROUTINE_HOME_ENTRY_KEY) {
                            RoutineHomeScreen(
                                navigateToAdd = {},
                                navigateToDetail = {},
                                navigateToFilter = {},
                                navigateToFinishedList = {},
                                componentVisibleProvider = { RoutineHomeScaffoldComponentVisible() },
                                homeReselectEvent = emptyFlow(),
                                routineViewModel = routineViewModel,
                                calendarViewModel = calendarViewModel,
                                holidayViewModel = holidayViewModel,
                                syncViewModel = syncViewModel,
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val ROUTINE_HOME_ENTRY_KEY = "RoutineHome"
    }
}
