package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-001 TC-SYNC-REFRESH-FEATURE-009 캘린더를 당기면 메모·태그와 공휴일, 날씨를 함께 새로고침한다`() {
        val holidayViewModel = holidayViewModel()
        val weatherViewModel = weatherViewModel()
        val syncViewModel = syncViewModel()
        setCalendarHomeScreen(
            holidayViewModel = holidayViewModel,
            weatherViewModel = weatherViewModel,
            syncViewModel = syncViewModel,
        )

        // 화면 표시 계기로 이미 요청된 공휴일·날씨 동기화와 구분해 당김으로 늘어난 요청을 확인한다.
        clearMocks(holidayViewModel, weatherViewModel, syncViewModel, answers = false)

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = JULY_2026) }
        verify(exactly = 1) { weatherViewModel.fetch() }
    }

    @Test
    fun `TC-SYNC-REFRESH-DOMAIN-003 새로고침 대상 중 하나라도 실행 중이면 진행 표시가 나타난다`() {
        setCalendarHomeScreen(
            holidayViewModel = holidayViewModel(isFetchingFlow = MutableStateFlow(true)),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-DOMAIN-007 날씨 동기화가 진행 중이면 진행 표시가 나타난다`() {
        setCalendarHomeScreen(
            weatherViewModel = weatherViewModel(isLoadingFlow = MutableStateFlow(true)),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-002 메모·태그 동기화가 진행 중이면 진행 표시가 나타난다`() {
        setCalendarHomeScreen(
            syncViewModel = syncViewModel(isRefreshingFlow = MutableStateFlow(true)),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-DOMAIN-004 새로고침 대상이 모두 끝나면 진행 표시가 사라진다`() {
        val isSyncRefreshing = MutableStateFlow(true)
        val isHolidayFetching = MutableStateFlow(true)
        val isWeatherLoading = MutableStateFlow(true)
        setCalendarHomeScreen(
            holidayViewModel = holidayViewModel(isFetchingFlow = isHolidayFetching),
            weatherViewModel = weatherViewModel(isLoadingFlow = isWeatherLoading),
            syncViewModel = syncViewModel(isRefreshingFlow = isSyncRefreshing),
        )
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        isSyncRefreshing.value = false
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        isHolidayFetching.value = false
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        isWeatherLoading.value = false
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-004 새로고침 대상이 모두 진행 중이 아니면 진행 표시가 나타나지 않는다`() {
        setCalendarHomeScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setCalendarHomeScreen(
        holidayViewModel: CalendarHomeHolidayViewModel = holidayViewModel(),
        weatherViewModel: CalendarHomeWeatherViewModel = weatherViewModel(),
        syncViewModel: CalendarHomeSyncViewModel = syncViewModel(),
    ) {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(emptyList())
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    state = state,
                    holidayViewModel = holidayViewModel,
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel,
                    syncViewModel = syncViewModel,
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        val JULY_2026: YearMonth = YearMonth(year = 2026, month = Month.JULY)
    }
}
