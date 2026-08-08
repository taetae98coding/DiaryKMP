package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.calendar.FIRST_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.SECOND_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.calendar.july
import io.github.taetae98coding.diary.compose.calendar.june
import io.github.taetae98coding.diary.compose.calendar.textItem
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
class CalendarWeekOfMonthGridTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-003 지정한 아이템이 표시된다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 8)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-004 주 시작일보다 이전 기간의 아이템은 표시되지 않는다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 1), endInclusive = july(day = 4)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-004 주 종료일보다 이후 기간의 아이템은 표시되지 않는다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 12), endInclusive = july(day = 14)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-005 주 시작일 이전에서 시작하는 아이템도 표시된다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 2), endInclusive = july(day = 6)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-005 주 종료일 이후까지 이어지는 아이템도 표시된다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 10), endInclusive = july(day = 15)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-005 주 전체를 덮는 7일 초과 기간의 아이템도 표시된다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = june(day = 29), endInclusive = july(day = 20)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-006 여러 그룹으로 나누어 지정한 아이템이 모두 표시된다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 8)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 7), endInclusive = july(day = 9)) }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-007 기간이 겹치는 아이템도 모두 표시된다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 8))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 7), endInclusive = july(day = 9))
            }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-008 아이템을 지정하지 않으면 날짜 한 줄만 표시된다`() {
        setCalendarWeekOfMonth {}

        allTexts() shouldBe listOf("5", "6", "7", "8", "9", "10", "11")
    }

    private fun setCalendarWeekOfMonth(content: CalendarWeekOfMonthGridScope.() -> Unit) {
        composeRule.setContent {
            DiaryTheme {
                CalendarWeekOfMonth(
                    yearMonth = YearMonth(year = 2026, month = Month.JULY),
                    weekOfMonth = 1,
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
}
