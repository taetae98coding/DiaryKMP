package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayOfWeek
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-e41da8ba-c7b2-48b4-a1c2-4645f1ff34ae 시작 달로 지정한 달의 캘린더를 여섯 주로 표시한다`() {
        setCalendar(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))

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
    fun `TC-e11ab367-e078-45c8-aab9-3bd429ca2980 시작 달을 지정하지 않으면 오늘이 속한 달을 표시한다`() {
        composeRule.setContent {
            DiaryTheme {
                Calendar {}
            }
        }

        dayTexts() shouldBe expectedDayTexts(Clock.System.todayIn(TimeZone.currentSystemDefault()).yearMonth)
    }

    @Test
    fun `TC-b41c60cf-c5e3-4cdc-938f-ae8251c3a9a4 스와이프로 다음 달로 이동한다`() {
        setCalendar(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        dayTexts() shouldBe expectedDayTexts(YearMonth(year = 2026, month = Month.AUGUST))
    }

    @Test
    fun `TC-af08ec81-5aa2-4cb2-a866-a134fafbf016 스와이프로 이전 달로 이동한다`() {
        setCalendar(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))

        composeRule.onRoot().performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        dayTexts() shouldBe expectedDayTexts(YearMonth(year = 2026, month = Month.JUNE))
    }

    @Test
    fun `TC-4e665a31-7c41-4c90-88c9-a634f520c2de 1년 1월에서 이전 달로 이동할 수 없다`() {
        setCalendar(initialYearMonth = YearMonth(year = 1, month = Month.JANUARY))

        composeRule.onRoot().performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        dayTexts() shouldBe expectedDayTexts(YearMonth(year = 1, month = Month.JANUARY))
    }

    @Test
    fun `TC-ad070fed-8717-422e-9ddd-6dca826f1649 화면이 재생성되어도 보던 달이 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                Calendar(state = rememberCalendarState(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))) {}
            }
        }
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        dayTexts() shouldBe expectedDayTexts(YearMonth(year = 2026, month = Month.AUGUST))
    }

    @Test
    fun `TC-f032e33b-8a2a-4c58-a9e4-ec4e177a41af 지정한 다른 해의 달로 이동을 요청하면 그 달의 캘린더가 표시된다`() {
        val state = CalendarState(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))

        composeRule.setContent {
            DiaryTheme {
                Calendar(state = state) {}
                LaunchedEffect(state) { state.animateScrollTo(YearMonth(year = 2027, month = Month.MARCH)) }
            }
        }
        composeRule.waitForIdle()

        state.currentYearMonth shouldBe YearMonth(year = 2027, month = Month.MARCH)
        dayTexts() shouldBe expectedDayTexts(YearMonth(year = 2027, month = Month.MARCH))
    }

    @Test
    fun `TC-f032e33b-8a2a-4c58-a9e4-ec4e177a41af 이동 범위의 첫 달로 이동을 요청하면 그 달의 캘린더가 표시된다`() {
        val state = CalendarState(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))

        composeRule.setContent {
            DiaryTheme {
                Calendar(state = state) {}
                LaunchedEffect(state) { state.animateScrollTo(YearMonth(year = 1, month = Month.JANUARY)) }
            }
        }
        composeRule.waitForIdle()

        state.currentYearMonth shouldBe YearMonth(year = 1, month = Month.JANUARY)
        dayTexts() shouldBe expectedDayTexts(YearMonth(year = 1, month = Month.JANUARY))
    }

    private fun setCalendar(initialYearMonth: YearMonth) {
        composeRule.setContent {
            DiaryTheme {
                Calendar(state = rememberCalendarState(initialYearMonth = initialYearMonth)) {}
            }
        }
    }

    private fun expectedDayTexts(yearMonth: YearMonth): List<String> {
        val sundayOfFirstWeek = yearMonth.firstDay.sundayOfWeek()

        return List(DAYS_PER_CALENDAR) { sundayOfFirstWeek.plus(it, DateTimeUnit.DAY).day.toString() }
    }

    private fun dayTexts(): List<String> =
        composeRule
            .onAllNodes(hasText(text = "", substring = true))
            .fetchSemanticsNodes()
            .map { node ->
                node.config[SemanticsProperties.Text].joinToString(separator = "") { it.text }
            }.filter { text -> text.isNotEmpty() && text.all(Char::isDigit) }

    companion object {
        private const val DAYS_PER_CALENDAR = 42
    }
}
