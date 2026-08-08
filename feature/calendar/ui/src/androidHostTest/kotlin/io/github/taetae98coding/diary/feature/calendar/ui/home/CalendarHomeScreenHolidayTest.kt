package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.permission.rememberLocationPermissionRequester
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
@Config(sdk = [36])
class CalendarHomeScreenHolidayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-DATA-001 화면이 표시되면 표시 중인 달의 연도 공휴일을 동기화한다`() {
        val yearMonth = YearMonth(year = 2026, month = Month.JULY)
        val holidayViewModel = holidayViewModel()

        setCalendarHomeScreen(
            initialYearMonth = yearMonth,
            holidayViewModel = holidayViewModel,
        )
        composeRule.waitForIdle()

        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = yearMonth) }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-002 표시 중인 달이 바뀌면 이동한 달의 연도 공휴일을 동기화한다`() {
        val initialYearMonth = YearMonth(year = 2026, month = Month.DECEMBER)
        val nextYearMonth = YearMonth(year = 2027, month = Month.JANUARY)
        val holidayViewModel = holidayViewModel()
        setCalendarHomeScreen(
            initialYearMonth = initialYearMonth,
            holidayViewModel = holidayViewModel,
        )
        composeRule.waitForIdle()

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = initialYearMonth) }
        verify(exactly = 1) { holidayViewModel.fetch(yearMonth = nextYearMonth) }
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        holidayViewModel: CalendarHomeHolidayViewModel,
    ) {
        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
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
                    memoViewModel = memoViewModel(),
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                    locationPermissionRequester = rememberLocationPermissionRequester(),
                )
            }
        }
    }

    private fun holidayViewModel(): CalendarHomeHolidayViewModel =
        mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.holidayList } returns MutableStateFlow(emptyList())
            every { viewModel.isFetching } returns MutableStateFlow(false)
        }

    private fun memoViewModel(): CalendarHomeMemoViewModel =
        mockk<CalendarHomeMemoViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.memoList } returns MutableStateFlow(emptyList())
            every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
        }
}
