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
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-005 주 시작일 이전에서 시작하는 아이템은 주 시작일부터 시작하는 아이템과 같은 자리를 차지한다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 2), endInclusive = july(day = 6)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 5), endInclusive = july(day = 6)) }
        }

        assertSameHorizontalBounds()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-005 주 종료일 이후까지 이어지는 아이템은 주 종료일에서 끝나는 아이템과 같은 자리를 차지한다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 10), endInclusive = july(day = 15)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 10), endInclusive = july(day = 11)) }
        }

        assertSameHorizontalBounds()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-005 주 전체를 덮는 아이템은 주 시작일부터 종료일까지 차지한다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = june(day = 29), endInclusive = july(day = 20)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 5), endInclusive = july(day = 11)) }
        }

        assertSameHorizontalBounds()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-006 같은 그룹에서 기간이 겹치지 않는 아이템은 같은 줄에 나란히 놓인다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 7))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 9), endInclusive = july(day = 10))
            }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
        val first = composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot()
        val second = composeRule.onNodeWithText(SECOND_ITEM_TEXT).getUnclippedBoundsInRoot()
        first.top shouldBe second.top
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-006 같은 그룹에서 기간이 하루라도 겹치는 아이템은 다른 줄에 놓인다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 8))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 8), endInclusive = july(day = 10))
            }
        }

        val first = composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot()
        val second = composeRule.onNodeWithText(SECOND_ITEM_TEXT).getUnclippedBoundsInRoot()
        first.top shouldNotBe second.top
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

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-007 지난주부터 이어져 일요일에 끝나는 아이템보다 다음 주까지 이어지는 아이템이 위쪽 줄에 놓인다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 2), endInclusive = july(day = 5))
                textItem(text = SECOND_ITEM_TEXT, start = june(day = 29), endInclusive = july(day = 20))
            }
        }

        assertSecondItemIsAboveFirstItem()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-007 잘린 기간의 시작일이 같으면 종료일이 늦은 아이템이 위쪽 줄에 놓인다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 6))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 8))
            }
        }

        assertSecondItemIsAboveFirstItem()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-007 잘린 기간이 같으면 잘리기 전 시작일이 이른 아이템이 위쪽 줄에 놓인다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 3), endInclusive = july(day = 6))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 1), endInclusive = july(day = 6))
            }
        }

        assertSecondItemIsAboveFirstItem()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-007 잘린 기간과 잘리기 전 시작일이 같으면 잘리기 전 종료일이 늦은 아이템이 위쪽 줄에 놓인다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 10), endInclusive = july(day = 12))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 10), endInclusive = july(day = 14))
            }
        }

        assertSecondItemIsAboveFirstItem()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-007 잘린 기간과 잘리기 전 기간이 모두 같으면 먼저 지정한 아이템이 위쪽 줄에 놓인다`() {
        setCalendarWeekOfMonth {
            group {
                textItem(text = FIRST_ITEM_TEXT, start = july(day = 7), endInclusive = july(day = 8))
                textItem(text = SECOND_ITEM_TEXT, start = july(day = 7), endInclusive = july(day = 8))
            }
        }

        assertFirstItemIsAboveSecondItem()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-014 기간이 겹치지 않아도 서로 다른 그룹의 아이템은 같은 줄을 공유하지 않고 앞 그룹이 위쪽에 놓인다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 7)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 9), endInclusive = july(day = 10)) }
        }

        assertFirstItemIsAboveSecondItem()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-014 뒤 그룹의 아이템이 더 먼저 시작해도 지정한 그룹 순서대로 위에서 아래로 놓인다`() {
        setCalendarWeekOfMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 9), endInclusive = july(day = 10)) }
            group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 7)) }
        }

        assertFirstItemIsAboveSecondItem()
    }

    private fun assertSecondItemIsAboveFirstItem() {
        val first = composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot()
        val second = composeRule.onNodeWithText(SECOND_ITEM_TEXT).getUnclippedBoundsInRoot()

        second.top shouldBeLessThan first.top
    }

    private fun assertFirstItemIsAboveSecondItem() {
        val first = composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot()
        val second = composeRule.onNodeWithText(SECOND_ITEM_TEXT).getUnclippedBoundsInRoot()

        first.top shouldBeLessThan second.top
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
