package io.github.taetae98coding.diary.compose.calendar.week

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
class CalendarWeekOfMonthTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-001 전월 날짜가 포함된 첫 주를 일요일부터 순서대로 표시한다`() {
        setCalendarWeekOfMonth(yearMonth = YearMonth(year = 2026, month = Month.JULY), weekOfMonth = 0)

        dayTexts() shouldBe listOf("28", "29", "30", "1", "2", "3", "4")
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-001 지정한 달 날짜로만 구성된 주를 일요일부터 순서대로 표시한다`() {
        setCalendarWeekOfMonth(yearMonth = YearMonth(year = 2026, month = Month.JULY), weekOfMonth = 1)

        dayTexts() shouldBe listOf("5", "6", "7", "8", "9", "10", "11")
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-001 1일이 일요일인 달의 첫 주를 1일부터 순서대로 표시한다`() {
        setCalendarWeekOfMonth(yearMonth = YearMonth(year = 2026, month = Month.FEBRUARY), weekOfMonth = 0)

        dayTexts() shouldBe listOf("1", "2", "3", "4", "5", "6", "7")
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-001 다음 달 날짜가 포함된 마지막 주를 일요일부터 순서대로 표시한다`() {
        setCalendarWeekOfMonth(yearMonth = YearMonth(year = 2026, month = Month.AUGUST), weekOfMonth = 5)

        dayTexts() shouldBe listOf("30", "31", "1", "2", "3", "4", "5")
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-002 범위를 벗어난 주 순서도 같은 기준으로 계산해 표시한다`() {
        setCalendarWeekOfMonth(yearMonth = YearMonth(year = 2026, month = Month.FEBRUARY), weekOfMonth = 4)

        dayTexts() shouldBe listOf("1", "2", "3", "4", "5", "6", "7")
    }

    @Test
    fun `음수 주 순서도 같은 기준으로 계산해 표시한다`() {
        setCalendarWeekOfMonth(yearMonth = YearMonth(year = 2026, month = Month.JULY), weekOfMonth = -1)

        dayTexts() shouldBe listOf("21", "22", "23", "24", "25", "26", "27")
    }

    private fun setCalendarWeekOfMonth(
        yearMonth: YearMonth,
        weekOfMonth: Int,
    ) {
        composeRule.setContent {
            DiaryTheme {
                CalendarWeekOfMonth(
                    yearMonth = yearMonth,
                    weekOfMonth = weekOfMonth,
                ) {}
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
}
