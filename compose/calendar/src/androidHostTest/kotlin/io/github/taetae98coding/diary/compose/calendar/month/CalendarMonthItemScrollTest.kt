package io.github.taetae98coding.diary.compose.calendar.month

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.calendar.july
import io.github.taetae98coding.diary.compose.calendar.scroll.CalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.textItem
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
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
@Config(sdk = [36])
class CalendarMonthItemScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val scrollToTopFlow = MutableStateFlow(false)

    @Test
    fun `TC-CALENDAR-MONTH-FEATURE-003 되돌리기를 요청하면 여섯 주가 모두 첫 아이템 줄부터 다시 보인다`() {
        val itemScrollState = CalendarItemScrollState()
        setCalendarMonth(itemScrollState = itemScrollState)
        scrollToLastItemOfEachWeek()
        composeRule.onNodeWithText(firstItemText(week = FIRST_WEEK)).isNotDisplayed() shouldBe true
        composeRule.onNodeWithText(firstItemText(week = SECOND_WEEK)).isNotDisplayed() shouldBe true

        scrollToTopFlow.value = true
        composeRule.waitForIdle()

        composeRule.onNodeWithText(firstItemText(week = FIRST_WEEK)).assertIsDisplayed()
        composeRule.onNodeWithText(firstItemText(week = SECOND_WEEK)).assertIsDisplayed()
    }

    private fun scrollToLastItemOfEachWeek() {
        listOf(FIRST_WEEK, SECOND_WEEK).forEach { week ->
            composeRule
                .onNode(hasScrollToNodeAction() and hasAnyDescendant(hasText(text = firstItemText(week = week))))
                .performScrollToIndex(ITEM_COUNT_PER_WEEK - 1)
        }
    }

    private fun setCalendarMonth(itemScrollState: CalendarItemScrollState) {
        composeRule.setContent {
            val isScrollToTopRequested by scrollToTopFlow.collectAsStateWithLifecycle()

            LaunchedEffect(isScrollToTopRequested) {
                if (isScrollToTopRequested) {
                    itemScrollState.scrollToTop()
                }
            }
            DiaryTheme {
                CalendarMonth(
                    yearMonth = YearMonth(year = 2026, month = Month.JULY),
                    modifier = Modifier.fillMaxSize(),
                    itemScrollState = itemScrollState,
                ) {
                    listOf(FIRST_WEEK, SECOND_WEEK).forEach { week ->
                        group {
                            repeat(ITEM_COUNT_PER_WEEK) { index ->
                                textItem(
                                    text = itemText(week = week, index = index),
                                    start = weekStart(week = week),
                                    endInclusive = weekEndInclusive(week = week),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun itemText(
        week: Int,
        index: Int,
    ): String = "Week${week}Item$index"

    private fun firstItemText(week: Int): String = itemText(week = week, index = 0)

    private fun lastItemText(week: Int): String = itemText(week = week, index = ITEM_COUNT_PER_WEEK - 1)

    private fun weekStart(week: Int): LocalDate = july(day = FIRST_SUNDAY_OF_JULY + week * DAYS_PER_WEEK)

    private fun weekEndInclusive(week: Int): LocalDate = july(day = FIRST_SUNDAY_OF_JULY + week * DAYS_PER_WEEK + DAYS_PER_WEEK - 1)

    companion object {
        private const val FIRST_WEEK = 1
        private const val SECOND_WEEK = 2
        private const val ITEM_COUNT_PER_WEEK = 6
        private const val FIRST_SUNDAY_OF_JULY = 5
        private const val DAYS_PER_WEEK = 7
    }
}
