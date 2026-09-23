package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.englishTitle
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenReselectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-010 캘린더 목적지를 다시 선택하면 오늘이 속한 달로 돌아간다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth
        val reselectEvent = reselectEvent()
        setCalendarHomeScreen(reselectEvent = reselectEvent)

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()

        reselectEvent.tryEmit(Unit)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-010 이미 오늘이 속한 달을 보고 있으면 보던 달을 유지한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val reselectEvent = reselectEvent()
        setCalendarHomeScreen(reselectEvent = reselectEvent)

        reselectEvent.tryEmit(Unit)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    private fun setCalendarHomeScreen(reselectEvent: MutableSharedFlow<Unit>) {
        val holidayViewModel = mockk<CalendarHomeHolidayViewModel>()
        every { holidayViewModel.fetch(any()) } returns Unit
        every { holidayViewModel.holidayList } returns MutableStateFlow(emptyList())
        every { holidayViewModel.isFetching } returns MutableStateFlow(false)

        val memoViewModel = mockk<CalendarHomeMemoViewModel>()
        every { memoViewModel.fetch(any()) } returns Unit
        every { memoViewModel.memoList } returns MutableStateFlow(emptyList())
        every { memoViewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())

        composeRule.setContent {
            DiaryTheme {
                // CalendarEntry가 조립하는 것과 같은 구성으로 둔다.
                val state = rememberCalendarHomeScaffoldState()

                ScrollToTodayOnReselectEffect(
                    reselectEvent = reselectEvent,
                    state = state,
                )
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    navigateToFilter = {},
                    state = state,
                    permissionManager = rememberPermissionManager(),
                    holidayViewModel = holidayViewModel,
                    memoViewModel = memoViewModel,
                    birthdayViewModel = birthdayViewModel(),
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                )
            }
        }
    }

    public companion object {
        private fun reselectEvent(): MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }
}
