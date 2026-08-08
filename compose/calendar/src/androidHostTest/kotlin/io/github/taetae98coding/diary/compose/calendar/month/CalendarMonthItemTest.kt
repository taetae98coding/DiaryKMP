package io.github.taetae98coding.diary.compose.calendar.month

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import io.github.taetae98coding.diary.compose.calendar.FIRST_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.august
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridScope
import io.github.taetae98coding.diary.compose.calendar.july
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
class CalendarMonthItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-MONTH-FEATURE-002 지정한 아이템이 겹치는 주에만 표시된다`() {
        setCalendarMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 1), endInclusive = july(day = 3)) }
        }

        itemNodeCount() shouldBe 1
    }

    @Test
    fun `TC-CALENDAR-MONTH-DOMAIN-002 여러 주에 걸친 아이템은 겹치는 각 주에 나뉘어 표시된다`() {
        setCalendarMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = july(day = 4), endInclusive = july(day = 5)) }
        }

        itemNodeCount() shouldBe 2
    }

    @Test
    fun `TC-CALENDAR-MONTH-DOMAIN-003 여섯 주와 겹치지 않는 아이템은 어느 주에도 표시되지 않는다`() {
        setCalendarMonth {
            group { textItem(text = FIRST_ITEM_TEXT, start = august(day = 9), endInclusive = august(day = 11)) }
        }

        itemNodeCount() shouldBe 0
    }

    private fun setCalendarMonth(content: CalendarWeekOfMonthGridScope.() -> Unit) {
        composeRule.setContent {
            DiaryTheme {
                CalendarMonth(
                    yearMonth = YearMonth(year = 2026, month = Month.JULY),
                    content = content,
                )
            }
        }
    }

    private fun itemNodeCount(): Int =
        composeRule
            .onAllNodesWithText(FIRST_ITEM_TEXT)
            .fetchSemanticsNodes()
            .size
}
