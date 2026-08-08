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
import io.github.taetae98coding.diary.compose.calendar.dayofweek.CalendarDayOfWeekRow
import io.github.taetae98coding.diary.compose.calendar.week.CalendarWeekOfMonth
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
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
class CalendarColorUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `밝은 테마 기본 색상을 제공한다`() {
        var colors: CalendarColor? = null
        composeRule.setContent {
            colors = CalendarDefault.colors(darkTheme = false)
        }

        composeRule.runOnIdle {
            colors shouldBe
                CalendarColor(
                    sundayAndHolidayColor = Color(color = 0xFFC62828),
                    saturdayColor = Color(color = 0xFF1565C0),
                )
        }
    }

    @Test
    fun `어두운 테마 기본 색상을 제공한다`() {
        var colors: CalendarColor? = null
        composeRule.setContent {
            colors = CalendarDefault.colors(darkTheme = true)
        }

        composeRule.runOnIdle {
            colors shouldBe
                CalendarColor(
                    sundayAndHolidayColor = Color(color = 0xFFFFCDD2),
                    saturdayColor = Color(color = 0xFFBBDEFB),
                )
        }
    }

    @Test
    fun `요일 헤더에 지정한 의미 색상을 적용한다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val defaultContentColor = fixtureMonkey.giveMeColor()
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    CalendarDayOfWeekRow(colors = colors)
                }
            }
        }

        composeRule.onNodeWithText("Sun").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("Mon").textColor() shouldBe defaultContentColor
        composeRule.onNodeWithText("Sat").textColor() shouldBe colors.saturdayColor
    }

    @Test
    fun `날짜에 요일 및 복수 공휴일 범위의 우선순위에 맞는 색상을 적용한다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val defaultContentColor = fixtureMonkey.giveMeColor()
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    Calendar(
                        state = rememberCalendarState(initialYearMonth = YearMonth(year = 2026, month = Month.JULY)),
                        holidayProvider = {
                            val firstHoliday = LocalDate(year = 2026, month = Month.JULY, day = 13)
                            val secondHolidayStart = LocalDate(year = 2026, month = Month.JULY, day = 15)
                            val secondHolidayEnd = LocalDate(year = 2026, month = Month.JULY, day = 16)

                            listOf(
                                firstHoliday..firstHoliday,
                                secondHolidayStart..secondHolidayEnd,
                            )
                        },
                        colors = colors,
                    ) {}
                }
            }
        }

        composeRule.onNodeWithText("12").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("13").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("14").textColor() shouldBe defaultContentColor
        composeRule.onNodeWithText("15").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("16").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("18").textColor() shouldBe colors.saturdayColor
    }

    @Test
    fun `공휴일 공급자가 변경되면 날짜 색상을 갱신한다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val defaultContentColor = fixtureMonkey.giveMeColor()
        val holiday = LocalDate(year = 2026, month = Month.JULY, day = 13)
        var holidayProvider: () -> List<LocalDateRange> by mutableStateOf({ emptyList() })
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    CalendarWeekOfMonth(
                        yearMonth = YearMonth(year = 2026, month = Month.JULY),
                        weekOfMonth = 2,
                        holidayProvider = holidayProvider,
                        colors = colors,
                    ) {}
                }
            }
        }

        composeRule.onNodeWithText("13").textColor() shouldBe defaultContentColor

        composeRule.runOnIdle {
            holidayProvider = { listOf(holiday..holiday) }
        }
        composeRule.onNodeWithText("13").textColor() shouldBe colors.sundayAndHolidayColor

        composeRule.runOnIdle {
            holidayProvider = { emptyList() }
        }
        composeRule.onNodeWithText("13").textColor() shouldBe defaultContentColor
    }

    @Test
    fun `이웃 달 날짜는 의미 색상을 유지한 채 강조를 낮춘다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val defaultContentColor = fixtureMonkey.giveMeColor()
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    CalendarWeekOfMonth(
                        yearMonth = YearMonth(year = 2026, month = Month.JULY),
                        weekOfMonth = 0,
                        colors = colors,
                    ) {}
                }
            }
        }

        composeRule.onNodeWithText("28").textColor() shouldBe
            colors.sundayAndHolidayColor.copy(alpha = colors.sundayAndHolidayColor.alpha * ADJACENT_MONTH_DAY_ALPHA)
        composeRule.onNodeWithText("29").textColor() shouldBe
            defaultContentColor.copy(alpha = defaultContentColor.alpha * ADJACENT_MONTH_DAY_ALPHA)
        composeRule.onNodeWithText("1").textColor() shouldBe defaultContentColor
        composeRule.onNodeWithText("4").textColor() shouldBe colors.saturdayColor
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
