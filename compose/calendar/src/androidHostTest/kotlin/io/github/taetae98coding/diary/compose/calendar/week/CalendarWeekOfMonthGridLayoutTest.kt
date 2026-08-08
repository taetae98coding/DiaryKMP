package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
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
class CalendarWeekOfMonthGridLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `주 시작일 이전에서 시작하는 아이템은 주 시작일부터 시작하는 아이템과 같은 자리를 차지한다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 2), endInclusive = july(day = 6)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 5), endInclusive = july(day = 6)) }
        }

        assertSameHorizontalBounds()
    }

    @Test
    fun `주 종료일 이후까지 이어지는 아이템은 주 종료일에서 끝나는 아이템과 같은 자리를 차지한다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 10), endInclusive = july(day = 15)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 10), endInclusive = july(day = 11)) }
        }

        assertSameHorizontalBounds()
    }

    @Test
    fun `주 전체를 덮는 아이템은 주 시작일부터 종료일까지 차지한다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = june(day = 29), endInclusive = july(day = 20)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 5), endInclusive = july(day = 11)) }
        }

        assertSameHorizontalBounds()
    }

    @Test
    fun `기간이 겹치지 않는 아이템도 모두 표시된다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 7))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 9), endInclusive = july(day = 10))
            }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
    }

    @Test
    fun `시작일 순서와 다르게 지정한 아이템도 모두 표시된다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 9), endInclusive = july(day = 10))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 7))
            }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
    }

    private fun assertSameHorizontalBounds() {
        val clipped = composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot()
        val expected = composeRule.onNodeWithText(SECOND_ITEM_TEXT).getUnclippedBoundsInRoot()

        clipped.left shouldBe expected.left
        clipped.right shouldBe expected.right
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
}
