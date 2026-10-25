package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.TODAY_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.englishTitle
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenPrimaryDateTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var onPrimary = Color.Unspecified

    @Test
    fun `TC-a4e13718-40dc-4525-8f9d-0f1028d9b57a 오늘 날짜가 주요 날짜로 강조된다`() {
        setCalendarHomeScreen(clock = clockAt(date = JULY_15_2026))

        composeRule.onNodeWithText(englishTitle(JULY_2026)).assertIsDisplayed()
        primaryColoredCount(text = JULY_15_2026.day.toString()) shouldBe 1
        primaryColoredTotalCount() shouldBe 1
    }

    @Test
    fun `TC-57d8ee56-0893-4073-8ebf-1dc6f5e9cbaf 오늘이 속하지 않은 달에서는 강조되는 날짜가 없다`() {
        setCalendarHomeScreen(clock = clockAt(date = JULY_15_2026))

        repeat(SWIPE_COUNT) { moveToNextMonth() }

        composeRule.onNodeWithText(englishTitle(SEPTEMBER_2026)).assertIsDisplayed()
        primaryColoredTotalCount() shouldBe 0
    }

    @Test
    fun `TC-df091414-1859-43e4-bc00-f7f4ce7d9d13 이웃 달 날짜로 함께 표시된 오늘도 강조된다`() {
        // 2026년 7월 캘린더의 첫 주는 6월 28일 일요일부터 시작하므로 6월 30일이 이웃 달 날짜로 함께 표시된다.
        setCalendarHomeScreen(clock = clockAt(date = JUNE_30_2026))

        moveToNextMonth()

        composeRule.onNodeWithText(englishTitle(JULY_2026)).assertIsDisplayed()
        primaryColoredCount(text = JUNE_30_2026.day.toString()) shouldBe 1
        primaryColoredTotalCount() shouldBe 1
    }

    @Test
    fun `TC-7c32957b-3e0f-4dfc-8380-440b40740d43 다른 앱에 다녀온 사이 날짜가 바뀌면 복귀 시 오늘 기준이 갱신된다`() {
        val clock = clockAt(date = JULY_31_2026)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setCalendarHomeScreen(clock = clock, lifecycleOwner = lifecycleOwner)
        primaryColoredCount(text = JULY_31_2026.day.toString()) shouldBe 1

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { every { clock.now() } returns noonOf(AUGUST_1_2026) }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertTodayMovedToAugustFirst()
    }

    @Test
    fun `TC-7c32957b-3e0f-4dfc-8380-440b40740d43 다른 화면에 다녀온 사이 날짜가 바뀌면 복귀 시 오늘 기준이 갱신된다`() {
        val clock = clockAt(date = JULY_31_2026)
        val screen = ReturnableScreen()
        setCalendarHomeScreen(clock = clock, screen = screen)
        primaryColoredCount(text = JULY_31_2026.day.toString()) shouldBe 1

        composeRule.runOnIdle { screen.isShown = false }
        composeRule.runOnIdle { every { clock.now() } returns noonOf(AUGUST_1_2026) }
        composeRule.runOnIdle { screen.isShown = true }
        composeRule.waitForIdle()

        assertTodayMovedToAugustFirst()
    }

    // 2026년 7월 캘린더의 마지막 주는 8월 1일 토요일까지 표시하므로, 보던 7월에서 바뀐 오늘이 강조되는지와 오늘 버튼의 목적지를 함께 확인한다.
    private fun assertTodayMovedToAugustFirst() {
        composeRule.onNodeWithText(englishTitle(JULY_2026)).assertIsDisplayed()
        primaryColoredCount(text = JULY_31_2026.day.toString()) shouldBe 0
        primaryColoredCount(text = AUGUST_1_2026.day.toString()) shouldBe 1
        primaryColoredTotalCount() shouldBe 1

        composeRule.onNodeWithContentDescription(TODAY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(AUGUST_2026)).assertIsDisplayed()
    }

    // 오늘 버튼도 오늘 날짜의 일 숫자를 표시하므로, 같은 숫자를 표시하는 노드 중 강조 색상을 쓰는 노드 수로 판정한다.
    private fun primaryColoredCount(text: String): Int {
        val nodes = composeRule.onAllNodesWithText(text, useUnmergedTree = true)

        return List(nodes.fetchSemanticsNodes().size) { index -> nodes[index].textColor() }
            .count { color -> color == onPrimary }
    }

    private fun primaryColoredTotalCount(): Int = (1..MAX_DAY_OF_MONTH).sumOf { day -> primaryColoredCount(text = day.toString()) }

    /** 다른 화면으로 이동하면 캘린더 홈 화면이 구성에서 빠지고, 돌아오면 보던 상태 그대로 다시 구성된다. */
    private class ReturnableScreen {
        var isShown by mutableStateOf(true)
    }

    private fun setCalendarHomeScreen(
        clock: Clock,
        lifecycleOwner: TestLifecycleOwner? = null,
        screen: ReturnableScreen = ReturnableScreen(),
    ) {
        val holidayViewModel = mockk<CalendarHomeHolidayViewModel>()
        every { holidayViewModel.fetch(any()) } returns Unit
        every { holidayViewModel.uiState } returns MutableStateFlow(CalendarHomeHolidayUiState())

        val memoViewModel = mockk<CalendarHomeMemoViewModel>()
        every { memoViewModel.select(any()) } returns Unit
        every { memoViewModel.uiState } returns MutableStateFlow(CalendarHomeMemoUiState())
        every { memoViewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())

        val birthdayViewModel = birthdayViewModel()
        val weatherViewModel = weatherViewModel()
        val syncViewModel = syncViewModel()

        composeRule.setContent {
            val calendarState = rememberCalendarState(initialYearMonth = clock.todayIn(TimeZone.currentSystemDefault()).yearMonth)
            val datePickerDialogState = rememberDialogState()
            val state =
                remember {
                    CalendarHomeScaffoldState(
                        calendarState = calendarState,
                        datePickerDialogState = datePickerDialogState,
                        snackbarHostState = SnackbarHostState(),
                        clock = clock,
                    )
                }

            CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                DiaryTheme {
                    CaptureOnPrimary()

                    if (screen.isShown) {
                        CalendarHomeScreen(
                            navigateToMemoDetail = {},
                            navigateToMemoAdd = {},
                            navigateToContactDetail = {},
                            birthdayViewModel = birthdayViewModel,
                            navigateToFilter = {},
                            navigateToTimetable = {},
                            holidayViewModel = holidayViewModel,
                            memoViewModel = memoViewModel,
                            weatherViewModel = weatherViewModel,
                            syncViewModel = syncViewModel,
                            state = state,
                            permissionManager = rememberPermissionManager(),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun CaptureOnPrimary() {
        onPrimary = DiaryTheme.colorScheme.onPrimary
    }

    private fun moveToNextMonth() {
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.textColor(): Color {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            action(textLayoutResults)
        }

        return textLayoutResults
            .single()
            .layoutInput.style.color
    }

    companion object {
        // 오늘이 속한 달의 캘린더에 오늘이 함께 표시될 여지를 없애기 위해 두 달 뒤로 이동한다.
        private const val SWIPE_COUNT = 2
        private const val MAX_DAY_OF_MONTH = 31
        private val JUNE_30_2026 = LocalDate(year = 2026, month = Month.JUNE, day = 30)
        private val JULY_15_2026 = LocalDate(year = 2026, month = Month.JULY, day = 15)
        private val JULY_31_2026 = LocalDate(year = 2026, month = Month.JULY, day = 31)
        private val AUGUST_1_2026 = LocalDate(year = 2026, month = Month.AUGUST, day = 1)
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val AUGUST_2026 = YearMonth(year = 2026, month = Month.AUGUST)
        private val SEPTEMBER_2026 = YearMonth(year = 2026, month = Month.SEPTEMBER)

        private fun noonOf(date: LocalDate): Instant = LocalDateTime(date = date, time = LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())

        private fun clockAt(date: LocalDate): Clock = mockk<Clock>().also { clock -> every { clock.now() } returns noonOf(date) }
    }
}
