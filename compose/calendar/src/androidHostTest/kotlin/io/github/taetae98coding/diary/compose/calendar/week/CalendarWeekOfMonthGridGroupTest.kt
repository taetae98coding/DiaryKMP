package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.calendar.FIRST_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.FOURTH_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.SECOND_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.THIRD_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.july
import io.github.taetae98coding.diary.compose.calendar.textItem
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarWeekOfMonthGridGroupTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-009 아이템을 지정하는 배치하는 화면에 표시 중인 주의 기간을 알린다`() {
        val dateRangeList = mutableListOf<LocalDateRange>()

        composeRule.setContent {
            DiaryTheme {
                CalendarWeekOfMonth(
                    yearMonth = JULY_2026,
                    weekOfMonth = 1,
                ) {
                    dateRangeList.add(dateRange)
                }
            }
        }

        dateRangeList.first() shouldBe july(day = 5)..july(day = 11)
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-010 배치하는 화면이 주마다 다른 그룹 구성을 지정하면 각 주의 아이템이 그대로 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                Column {
                    CalendarWeekOfMonth(
                        yearMonth = JULY_2026,
                        weekOfMonth = 1,
                        modifier = Modifier.weight(1F),
                    ) {
                        group {
                            textItem(text = FIRST_ITEM_TEXT, start = july(day = 6), endInclusive = july(day = 6))
                            textItem(text = SECOND_ITEM_TEXT, start = july(day = 9), endInclusive = july(day = 9))
                        }
                    }
                    CalendarWeekOfMonth(
                        yearMonth = JULY_2026,
                        weekOfMonth = 2,
                        modifier = Modifier.weight(1F),
                    ) {
                        group { textItem(text = THIRD_ITEM_TEXT, start = july(day = 13), endInclusive = july(day = 13)) }
                        group { textItem(text = FOURTH_ITEM_TEXT, start = july(day = 16), endInclusive = july(day = 16)) }
                    }
                }
            }
        }

        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(THIRD_ITEM_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(FOURTH_ITEM_TEXT).assertIsDisplayed()
    }

    companion object {
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
