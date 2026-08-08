package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.week.CalendarWeekOfMonth
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarPrimaryDateUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `주요 날짜의 숫자는 주 색상 위에서 읽기 좋은 색으로 표시된다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        var onPrimary = Color.Unspecified

        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    CalendarWeekOfMonth(
                        yearMonth = YearMonth(year = 2026, month = Month.JULY),
                        weekOfMonth = 2,
                        primaryDateProvider = { listOf(LocalDate(year = 2026, month = Month.JULY, day = 15)) },
                    ) {}
                }
            }
        }

        composeRule.onNodeWithText("15").textColor() shouldBe onPrimary
        composeRule.onNodeWithText("14").textColor() shouldBe defaultContentColor
    }

    @Test
    fun `주요 날짜는 요일과 공휴일보다 앞선 색상 우선순위를 가진다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val sunday = LocalDate(year = 2026, month = Month.JULY, day = 12)
        val holiday = LocalDate(year = 2026, month = Month.JULY, day = 13)
        val saturday = LocalDate(year = 2026, month = Month.JULY, day = 18)
        var onPrimary = Color.Unspecified

        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CalendarWeekOfMonth(
                    yearMonth = YearMonth(year = 2026, month = Month.JULY),
                    weekOfMonth = 2,
                    holidayProvider = { listOf(holiday..holiday) },
                    primaryDateProvider = { listOf(sunday, holiday, saturday) },
                    colors = colors,
                ) {}
            }
        }

        composeRule.onNodeWithText("12").textColor() shouldBe onPrimary
        composeRule.onNodeWithText("13").textColor() shouldBe onPrimary
        composeRule.onNodeWithText("18").textColor() shouldBe onPrimary
    }

    @Test
    fun `주요 날짜 공급자가 변경되면 날짜 색상을 갱신한다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        val primaryDate = LocalDate(year = 2026, month = Month.JULY, day = 15)
        var primaryDateProvider: () -> List<LocalDate> by mutableStateOf({ emptyList() })
        var onPrimary = Color.Unspecified

        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    CalendarWeekOfMonth(
                        yearMonth = YearMonth(year = 2026, month = Month.JULY),
                        weekOfMonth = 2,
                        primaryDateProvider = primaryDateProvider,
                    ) {}
                }
            }
        }

        composeRule.onNodeWithText("15").textColor() shouldBe defaultContentColor

        composeRule.runOnIdle {
            primaryDateProvider = { listOf(primaryDate) }
        }
        composeRule.onNodeWithText("15").textColor() shouldBe onPrimary

        composeRule.runOnIdle {
            primaryDateProvider = { emptyList() }
        }
        composeRule.onNodeWithText("15").textColor() shouldBe defaultContentColor
    }

    @Test
    fun `같은 주요 날짜를 여러 번 지정해도 한 번 지정한 것과 같다`() {
        val primaryDate = LocalDate(year = 2026, month = Month.JULY, day = 15)
        var onPrimary = Color.Unspecified

        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CalendarWeekOfMonth(
                    yearMonth = YearMonth(year = 2026, month = Month.JULY),
                    weekOfMonth = 2,
                    primaryDateProvider = { listOf(primaryDate, primaryDate, primaryDate) },
                ) {}
            }
        }

        composeRule.onNodeWithText("15").textColor() shouldBe onPrimary
    }

    @Test
    fun `이웃 달 주요 날짜는 강조를 낮추지 않는다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        val primaryDate = LocalDate(year = 2026, month = Month.JUNE, day = 28)
        var onPrimary = Color.Unspecified

        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    CalendarWeekOfMonth(
                        yearMonth = YearMonth(year = 2026, month = Month.JULY),
                        weekOfMonth = 0,
                        primaryDateProvider = { listOf(primaryDate) },
                    ) {}
                }
            }
        }

        composeRule.onNodeWithText("28").textColor() shouldBe onPrimary
        composeRule.onNodeWithText("29").textColor() shouldBe
            defaultContentColor.copy(alpha = defaultContentColor.alpha * ADJACENT_MONTH_DAY_ALPHA)
    }

    private fun SemanticsNodeInteraction.textColor(): Color {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            action(textLayoutResults)
        }

        return textLayoutResults
            .single()
            .layoutInput.style.color
    }

    companion object {
        private const val ADJACENT_MONTH_DAY_ALPHA = 0.38F
    }
}

private fun FixtureMonkey.giveMeCalendarColor(): CalendarColor =
    CalendarColor(
        sundayAndHolidayColor = giveMeColor(),
        saturdayColor = giveMeColor(),
    )

private fun FixtureMonkey.giveMeColor(): Color = Color(color = giveMeOne<Int>())
