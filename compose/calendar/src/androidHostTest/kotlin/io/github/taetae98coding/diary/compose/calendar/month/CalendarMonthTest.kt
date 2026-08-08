package io.github.taetae98coding.diary.compose.calendar.month

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
class CalendarMonthTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-MONTH-FEATURE-001 지정한 달의 여섯 주를 표시한다`() {
        setCalendarMonth(yearMonth = YearMonth(year = 2026, month = Month.JULY))

        dayTexts() shouldBe
            listOf(
                "28",
                "29",
                "30",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14",
                "15",
                "16",
                "17",
                "18",
                "19",
                "20",
                "21",
                "22",
                "23",
                "24",
                "25",
                "26",
                "27",
                "28",
                "29",
                "30",
                "31",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
            )
    }

    @Test
    fun `TC-CALENDAR-MONTH-DOMAIN-001 주 수가 여섯보다 적은 달은 남는 주가 다음 달 날짜로만 표시된다`() {
        setCalendarMonth(yearMonth = YearMonth(year = 2026, month = Month.FEBRUARY))

        dayTexts() shouldBe (1..LAST_DAY_OF_FEBRUARY_2026).map(Int::toString) + (1..LAST_DAY_OF_REMAINING_WEEKS_IN_MARCH).map(Int::toString)
    }

    private fun setCalendarMonth(yearMonth: YearMonth) {
        composeRule.setContent {
            DiaryTheme {
                CalendarMonth(yearMonth = yearMonth) {}
            }
        }
    }

    private fun dayTexts(): List<String> =
        composeRule
            .onAllNodes(hasText(text = "", substring = true))
            .fetchSemanticsNodes()
            .map { node ->
                node.config[SemanticsProperties.Text].joinToString(separator = "") { it.text }
            }

    companion object {
        private const val LAST_DAY_OF_FEBRUARY_2026 = 28
        private const val LAST_DAY_OF_REMAINING_WEEKS_IN_MARCH = 14
    }
}
