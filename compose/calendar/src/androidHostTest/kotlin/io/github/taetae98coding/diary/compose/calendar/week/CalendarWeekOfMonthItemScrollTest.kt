package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.calendar.FIRST_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.SECOND_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.july
import io.github.taetae98coding.diary.compose.calendar.scroll.CalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.textItem
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
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
class CalendarWeekOfMonthItemScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val scrollToTopFlow = MutableStateFlow(false)

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-011 되돌리기를 요청하면 첫 아이템 줄부터 다시 보인다`() {
        val itemScrollState = CalendarItemScrollState()
        setCalendarWeekOfMonth(
            itemScrollState = itemScrollState,
            hasLeadingGroupFlow = MutableStateFlow(false),
        )
        composeRule
            .onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText(text = lastFilledItemText()))
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).isNotDisplayed() shouldBe true

        requestScrollToTop()

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-001 되돌리기 요청이 없으면 위쪽에 생긴 아이템은 바로 보이지 않는다`() {
        val hasLeadingGroupFlow = MutableStateFlow(false)
        setCalendarWeekOfMonth(
            itemScrollState = CalendarItemScrollState(),
            hasLeadingGroupFlow = hasLeadingGroupFlow,
        )
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()

        hasLeadingGroupFlow.value = true
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SECOND_ITEM_TEXT).isNotDisplayed() shouldBe true
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-002 되돌리기를 요청하면 위쪽에 생긴 아이템이 바로 보인다`() {
        val itemScrollState = CalendarItemScrollState()
        val hasLeadingGroupFlow = MutableStateFlow(false)
        setCalendarWeekOfMonth(
            itemScrollState = itemScrollState,
            hasLeadingGroupFlow = hasLeadingGroupFlow,
        )
        hasLeadingGroupFlow.value = true
        composeRule.waitForIdle()

        requestScrollToTop()

        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
    }

    private fun requestScrollToTop() {
        scrollToTopFlow.value = true
        composeRule.waitForIdle()
    }

    private fun setCalendarWeekOfMonth(
        itemScrollState: CalendarItemScrollState,
        hasLeadingGroupFlow: MutableStateFlow<Boolean>,
    ) {
        composeRule.setContent {
            val hasLeadingGroup by hasLeadingGroupFlow.collectAsStateWithLifecycle()
            val isScrollToTopRequested by scrollToTopFlow.collectAsStateWithLifecycle()

            LaunchedEffect(isScrollToTopRequested) {
                if (isScrollToTopRequested) {
                    itemScrollState.scrollToTop()
                }
            }

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
                    if (hasLeadingGroup) {
                        group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 5), endInclusive = july(day = 11)) }
                    }
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

    private fun filledItemText(index: Int): String = if (index == 0) FIRST_ITEM_TEXT else "$FILLED_ITEM_TEXT_PREFIX$index"

    private fun lastFilledItemText(): String = filledItemText(index = FILLED_ITEM_COUNT - 1)

    companion object {
        private const val FILLED_ITEM_COUNT = 6
        private const val FILLED_ITEM_TEXT_PREFIX = "FilledCalendarItem"
        private val WEEK_HEIGHT = 96.dp
    }
}
