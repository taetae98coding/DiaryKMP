package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.text.TextLayoutResult
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenPrimaryDateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `캘린더의 오늘 날짜가 주요 날짜로 강조된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        var onPrimary = Color.Unspecified

        setCalendarHomeScreen { onPrimary = DiaryTheme.colorScheme.onPrimary }

        primaryColoredCount(text = today.day.toString(), onPrimary = onPrimary) shouldBe 1
    }

    @Test
    fun `오늘이 속하지 않은 달로 이동하면 강조되는 날짜가 없다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        var onPrimary = Color.Unspecified

        setCalendarHomeScreen { onPrimary = DiaryTheme.colorScheme.onPrimary }

        repeat(SWIPE_COUNT) {
            composeRule.onRoot().performTouchInput { swipeLeft() }
            composeRule.waitForIdle()
        }

        primaryColoredCount(text = today.day.toString(), onPrimary = onPrimary) shouldBe 0
    }

    // 오늘 버튼도 오늘 날짜의 일 숫자를 표시하므로, 같은 숫자를 표시하는 노드 중 강조 색상을 쓰는 노드 수로 판정한다.
    private fun primaryColoredCount(
        text: String,
        onPrimary: Color,
    ): Int {
        val nodes = composeRule.onAllNodesWithText(text, useUnmergedTree = true)

        return List(nodes.fetchSemanticsNodes().size) { index -> nodes[index].textColor() }
            .count { color -> color == onPrimary }
    }

    private fun setCalendarHomeScreen(onTheme: @Composable () -> Unit) {
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
                onTheme()

                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
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
    }
}
