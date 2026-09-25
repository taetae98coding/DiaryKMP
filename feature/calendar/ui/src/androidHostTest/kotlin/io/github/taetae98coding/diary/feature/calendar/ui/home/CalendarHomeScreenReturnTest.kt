package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-DATA-041 다른 앱에서 돌아오면 현재 위치의 날씨를 다시 동기화한다`() {
        val weatherViewModel = weatherViewModel()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setCalendarHomeScreen(weatherViewModel = weatherViewModel, lifecycleOwner = lifecycleOwner)
        composeRule.waitForIdle()
        verify(exactly = 1) { weatherViewModel.fetch() }

        leaveToOtherAppAndReturn(lifecycleOwner)

        verify(exactly = 2) { weatherViewModel.fetch() }
        verify(exactly = 0) { weatherViewModel.refreshOnLocationPermissionGranted() }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-041 다른 화면에서 돌아오면 현재 위치의 날씨를 다시 동기화한다`() {
        val weatherViewModel = weatherViewModel()
        val screen = setCalendarHomeScreen(weatherViewModel = weatherViewModel)
        composeRule.waitForIdle()
        verify(exactly = 1) { weatherViewModel.fetch() }

        leaveToOtherScreenAndReturn(screen)

        verify(exactly = 2) { weatherViewModel.fetch() }
        verify(exactly = 0) { weatherViewModel.refreshOnLocationPermissionGranted() }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-042 다른 앱에 다녀오는 것만으로는 공휴일을 다시 동기화하지 않는다`() {
        val holidayViewModel = holidayViewModel()
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setCalendarHomeScreen(
            holidayViewModel = holidayViewModel,
            initialYearMonth = JULY_2026,
            lifecycleOwner = lifecycleOwner,
        )
        composeRule.waitForIdle()
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = JULY_2026) }

        leaveToOtherAppAndReturn(lifecycleOwner)

        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = any()) }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-043 다른 화면에서 돌아오면 그 시점의 표시 중인 달을 기준으로 공휴일을 다시 동기화하고 표시한다`() {
        val holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(listOf(winterHoliday())))
        val screen = setCalendarHomeScreen(holidayViewModel = holidayViewModel, initialYearMonth = DECEMBER_2026)
        moveToNextMonth()
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = JANUARY_2027) }

        leaveToOtherScreenAndReturn(screen)

        verify(exactly = 2) { holidayViewModel.fetch(yearMonth = JANUARY_2027) }
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JANUARY_2027)).assertIsDisplayed()
        composeRule.onNodeWithText(WINTER_HOLIDAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-044 화면이 재생성되면 그 시점의 표시 중인 달을 기준으로 공휴일을 다시 동기화하고 표시한다`() {
        val holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(listOf(winterHoliday())))
        val restorationTester = StateRestorationTester(composeRule)
        setCalendarHomeScreen(
            holidayViewModel = holidayViewModel,
            initialYearMonth = DECEMBER_2026,
            restorationTester = restorationTester,
        )
        moveToNextMonth()
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = JANUARY_2027) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        verify(exactly = 2) { holidayViewModel.fetch(yearMonth = JANUARY_2027) }
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JANUARY_2027)).assertIsDisplayed()
        composeRule.onNodeWithText(WINTER_HOLIDAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-045 다른 화면에서 돌아오면 그 시점의 표시 중인 달을 기준으로 음력 자료를 다시 동기화한다`() {
        val fetchLunarUseCase = fetchLunarUseCase()
        val screen =
            setCalendarHomeScreen(
                birthdayViewModel = lunarObservingBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase),
                initialYearMonth = DECEMBER_2026,
            )
        moveToNextMonth()
        clearMocks(fetchLunarUseCase, answers = false)

        leaveToOtherScreenAndReturn(screen)

        coVerify(exactly = 1) { fetchLunarUseCase(parameter = 2026) }
        coVerify(exactly = 1) { fetchLunarUseCase(parameter = 2027) }
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JANUARY_2027)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-046 화면이 재생성되면 그 시점의 표시 중인 달을 기준으로 음력 자료를 다시 동기화한다`() {
        val fetchLunarUseCase = fetchLunarUseCase()
        val restorationTester = StateRestorationTester(composeRule)
        setCalendarHomeScreen(
            birthdayViewModel = lunarObservingBirthdayViewModel(fetchLunarUseCase = fetchLunarUseCase),
            initialYearMonth = DECEMBER_2026,
            restorationTester = restorationTester,
        )
        moveToNextMonth()
        clearMocks(fetchLunarUseCase, answers = false)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { fetchLunarUseCase(parameter = 2026) }
        coVerify(exactly = 1) { fetchLunarUseCase(parameter = 2027) }
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JANUARY_2027)).assertIsDisplayed()
    }

    /** 다른 화면으로 이동하면 캘린더 홈 화면이 구성에서 빠지고, 돌아오면 보던 상태 그대로 다시 구성된다. */
    private class ReturnableScreen {
        var isShown by mutableStateOf(true)
    }

    private fun setCalendarHomeScreen(
        holidayViewModel: CalendarHomeHolidayViewModel = holidayViewModel(),
        weatherViewModel: CalendarHomeWeatherViewModel = weatherViewModel(),
        birthdayViewModel: CalendarHomeBirthdayViewModel = birthdayViewModel(),
        initialYearMonth: YearMonth = JULY_2026,
        lifecycleOwner: TestLifecycleOwner? = null,
        restorationTester: StateRestorationTester? = null,
    ): ReturnableScreen {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(emptyList())
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }
        val syncViewModel = syncViewModel()
        val screen = ReturnableScreen()
        val setContent: (@Composable () -> Unit) -> Unit =
            restorationTester?.let { tester -> { content -> tester.setContent(content) } } ?: composeRule::setContent

        setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
                )

            CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                DiaryTheme {
                    if (screen.isShown) {
                        CalendarHomeScreen(
                            navigateToMemoDetail = {},
                            navigateToMemoAdd = {},
                            navigateToContactDetail = {},
                            navigateToFilter = {},
                            state = state,
                            permissionManager = rememberPermissionManager(),
                            holidayViewModel = holidayViewModel,
                            memoViewModel = memoViewModel,
                            birthdayViewModel = birthdayViewModel,
                            weatherViewModel = weatherViewModel,
                            syncViewModel = syncViewModel,
                        )
                    }
                }
            }
        }

        return screen
    }

    private fun moveToNextMonth() {
        composeRule.waitForIdle()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun leaveToOtherAppAndReturn(lifecycleOwner: TestLifecycleOwner) {
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()
    }

    private fun leaveToOtherScreenAndReturn(screen: ReturnableScreen) {
        composeRule.runOnIdle { screen.isShown = false }
        composeRule.runOnIdle { screen.isShown = true }
        composeRule.waitForIdle()
    }

    private fun winterHoliday(): Holiday =
        Holiday(
            name = WINTER_HOLIDAY_NAME,
            isHoliday = true,
            dateRange = LocalDate(year = 2027, month = Month.JANUARY, day = 15)..LocalDate(year = 2027, month = Month.JANUARY, day = 15),
        )

    companion object {
        private const val WINTER_HOLIDAY_NAME = "겨울 축제"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val DECEMBER_2026 = YearMonth(year = 2026, month = Month.DECEMBER)
        private val JANUARY_2027 = YearMonth(year = 2027, month = Month.JANUARY)
    }
}
