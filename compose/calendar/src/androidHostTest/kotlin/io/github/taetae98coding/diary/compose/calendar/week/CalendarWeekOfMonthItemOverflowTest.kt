package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.FIRST_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.july
import io.github.taetae98coding.diary.compose.calendar.scroll.CalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.textItem
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarWeekOfMonthItemOverflowTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val itemScrollState = CalendarItemScrollState()
    private lateinit var scope: CoroutineScope

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-012 아이템이 한 번에 모두 보이지 않으면 스크롤해 나머지 아이템을 확인한다`() {
        setCalendarWeekOfMonth()
        composeRule.onNodeWithText(LAST_ITEM_TEXT).isNotDisplayed() shouldBe true

        scrollToLastItem()

        composeRule.onNodeWithText(LAST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-013 되돌린 뒤에도 다시 스크롤해 나머지 아이템을 확인한다`() {
        setCalendarWeekOfMonth()
        scrollToLastItem()
        requestScrollToTop()
        composeRule.onNodeWithText(LAST_ITEM_TEXT).isNotDisplayed() shouldBe true

        scrollToLastItem()

        composeRule.onNodeWithText(LAST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-003 이미 첫 아이템 줄부터 보이는 상태에서 되돌리기를 요청해도 표시가 달라지지 않는다`() {
        setCalendarWeekOfMonth()
        val before = composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot()

        requestScrollToTop()

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot() shouldBe before
        composeRule.onNodeWithText(LAST_ITEM_TEXT).isNotDisplayed() shouldBe true
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-004 되돌리기는 요청할 때마다 적용된다`() {
        setCalendarWeekOfMonth()
        scrollToLastItem()
        requestScrollToTop()
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()

        scrollToLastItem()
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).isNotDisplayed() shouldBe true
        requestScrollToTop()

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    private fun scrollToLastItem() {
        composeRule
            .onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText(text = LAST_ITEM_TEXT))
        composeRule.waitForIdle()
    }

    private fun requestScrollToTop() {
        composeRule.runOnIdle { scope.launch { itemScrollState.scrollToTop() } }
        composeRule.waitForIdle()
    }

    private fun setCalendarWeekOfMonth() {
        composeRule.setContent {
            scope = rememberCoroutineScope()

            DiaryTheme {
                CalendarWeekOfMonth(
                    yearMonth = YearMonth(year = 2026, month = Month.JULY),
                    weekOfMonth = 1,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(WEEK_HEIGHT),
                    itemScrollState = itemScrollState,
                ) {
                    group {
                        repeat(FILLED_ITEM_COUNT) { index ->
                            textItem(
                                text = filledItemText(index = index),
                                start = july(day = 5),
                                endInclusive = july(day = 11),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun filledItemText(index: Int): String =
        when (index) {
            0 -> FIRST_ITEM_TEXT
            FILLED_ITEM_COUNT - 1 -> LAST_ITEM_TEXT
            else -> "$FILLED_ITEM_TEXT_PREFIX$index"
        }

    companion object {
        private const val FILLED_ITEM_COUNT = 6
        private const val FILLED_ITEM_TEXT_PREFIX = "FilledCalendarItem"
        private const val LAST_ITEM_TEXT = "LastCalendarItem"
        private val WEEK_HEIGHT = 96.dp
    }
}
