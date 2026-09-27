package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.TODAY_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.englishTitle
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.mockk.every
import io.mockk.mockk
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
class CalendarHomeScreenTodayButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val minimumTouchSize = 48.dp

    @Test
    fun `상단 바 오른쪽에 오늘 날짜의 일 숫자가 표시된 오늘 버튼이 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScreen()

        composeRule
            .onNode(hasContentDescription(TODAY_BUTTON_DESCRIPTION) and hasText(today.day.toString()))
            .assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-015 다른 달을 보는 중에 오늘 버튼을 누르면 오늘이 속한 달로 이동한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth

        setCalendarHomeScreen()

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(TODAY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-016 오늘이 속한 달을 보는 중에 오늘 버튼을 누르면 보던 달이 유지된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScreen()

        composeRule.onNodeWithContentDescription(TODAY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    @Test
    fun `오늘 버튼을 누를 수 있는 영역은 최소 터치 크기를 유지한다`() {
        setCalendarHomeScreen()

        composeRule
            .onNodeWithContentDescription(TODAY_BUTTON_DESCRIPTION)
            .assertTouchWidthIsEqualTo(minimumTouchSize)
            .assertTouchHeightIsEqualTo(minimumTouchSize)
    }

    private fun setCalendarHomeScreen() {
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
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    navigateToTimetable = {},
                    holidayViewModel = holidayViewModel,
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                    state = rememberCalendarHomeScaffoldState(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }
}
