package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenSelectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-035 날짜 칸을 길게 눌러 선택을 완료하면 그 하루로 MemoAdd 화면 이동이 요청된다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setCalendarHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })

        performLongPress(dayCenter(day = 15))
        performUp()

        navigatedDateRangeList shouldBe listOf(july(day = 15)..july(day = 15))
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-036 드래그로 기간을 선택해 완료하면 그 기간으로 MemoAdd 화면 이동이 요청된다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setCalendarHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })

        performLongPress(dayCenter(day = 14))
        performMoveTo(dayCenter(day = 17))
        performUp()

        navigatedDateRangeList shouldBe listOf(july(day = 14)..july(day = 17))
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-092 기간을 선택하는 도중 시스템이 선택을 중단하면 그 기간으로 MemoAdd 화면 이동이 요청된다`() {
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setCalendarHomeScreen(navigateToMemoAdd = { navigatedDateRangeList += it })

        performLongPress(dayCenter(day = 14))
        performMoveTo(dayCenter(day = 17))
        composeRule.onRoot().performTouchInput { cancel() }
        composeRule.waitForIdle()

        navigatedDateRangeList shouldBe listOf(july(day = 14)..july(day = 17))
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-093 기간을 선택하는 도중 화면이 재생성되면 MemoAdd 화면 이동이 요청되지 않고 선택이 남지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        var capturedState: CalendarHomeScaffoldState? = null
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setCalendarHomeScreen(
            navigateToMemoAdd = { navigatedDateRangeList += it },
            onState = { capturedState = it },
            restorationTester = restorationTester,
        )

        performLongPress(dayCenter(day = 14))
        performMoveTo(dayCenter(day = 17))
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        navigatedDateRangeList shouldBe emptyList()
        capturedState?.calendarSelectState?.dateRange.shouldBeNull()
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JULY_2026)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-047 MemoAdd 화면으로 이동하면 캘린더의 선택이 해제된다`() {
        var capturedState: CalendarHomeScaffoldState? = null
        val navigatedDateRangeList = mutableListOf<LocalDateRange>()
        setCalendarHomeScreen(
            navigateToMemoAdd = { navigatedDateRangeList += it },
            onState = { capturedState = it },
        )

        performLongPress(dayCenter(day = 15))
        performUp()

        navigatedDateRangeList.size shouldBe 1
        capturedState?.calendarSelectState?.dateRange.shouldBeNull()
    }

    private fun setCalendarHomeScreen(
        navigateToMemoAdd: (LocalDateRange) -> Unit = {},
        onState: (CalendarHomeScaffoldState) -> Unit = {},
        restorationTester: StateRestorationTester? = null,
    ) {
        val holidayViewModel =
            mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.holidayList } returns MutableStateFlow(emptyList())
                every { viewModel.isFetching } returns MutableStateFlow(false)
            }
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(emptyList())
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        val setContent: (@Composable () -> Unit) -> Unit =
            restorationTester?.let { tester -> { content -> tester.setContent(content) } } ?: composeRule::setContent

        setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )
            onState(state)

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = navigateToMemoAdd,
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    state = state,
                    holidayViewModel = holidayViewModel,
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    // 상단 바 오늘 버튼도 일 숫자를 표시하므로 클릭할 수 없는 날짜 숫자만 대상으로 삼는다.
    private fun dayCenter(day: Int): Offset =
        composeRule
            .onAllNodes(hasText(day.toString()).and(hasNoClickAction()))[0]
            .fetchSemanticsNode()
            .boundsInRoot
            .center

    private fun performLongPress(position: Offset) {
        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
        }
        composeRule.waitForIdle()
    }

    private fun performMoveTo(position: Offset) {
        composeRule.onRoot().performTouchInput { moveTo(position) }
        composeRule.waitForIdle()
    }

    private fun performUp() {
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitForIdle()
    }

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private const val LONG_PRESS_MARGIN_MILLIS = 100L
    }
}
