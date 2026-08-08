package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-FEATURE-009 배치하는 화면이 지정한 아이템이 표시된다`() {
        setCalendar {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 8)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-FEATURE-010 아이템을 지정하지 않으면 날짜만 표시된다`() {
        setCalendar {}

        allTexts().size shouldBe DAY_OF_WEEK_COUNT + DAYS_PER_CALENDAR
    }

    @Test
    fun `TC-CALENDAR-FEATURE-011 아이템 지정이 바뀌면 표시가 갱신된다`() {
        var itemText by mutableStateOf(FIRST_ITEM_TEXT)
        composeRule.setContent {
            DiaryTheme {
                Calendar(state = rememberCalendarState(initialYearMonth = JULY_2026)) {
                    group { textItem(text = itemText, start = july(day = 6), endInclusive = july(day = 8)) }
                }
            }
        }
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()

        composeRule.runOnIdle { itemText = SECOND_ITEM_TEXT }

        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-FEATURE-012 달을 이동하면 이동한 달과 겹치는 아이템만 표시된다`() {
        setCalendar {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 8))
                textItem(text = SECOND_ITEM_TEXT, start = august(day = 3), endInclusive = august(day = 5))
            }
        }

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsNotDisplayed()
    }

    private fun setCalendar(content: CalendarWeekOfMonthGridScope.() -> Unit) {
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    content = content,
                )
            }
        }
    }

    private fun allTexts(): List<String> =
        composeRule
            .onAllNodes(hasText(text = "", substring = true))
            .fetchSemanticsNodes()
            .map { node ->
                node.config[SemanticsProperties.Text].joinToString(separator = "") { it.text }
            }

    companion object {
        private const val DAY_OF_WEEK_COUNT = 7
        private const val DAYS_PER_CALENDAR = 42
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
