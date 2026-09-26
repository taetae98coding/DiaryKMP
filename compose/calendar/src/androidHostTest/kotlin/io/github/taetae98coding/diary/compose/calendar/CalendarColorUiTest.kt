package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    fun `TC-CALENDAR-FEATURE-019 지정한 공휴일 기간에 드는 날짜가 공휴일로 구분된다`() {
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
    fun `TC-CALENDAR-DOMAIN-004 공휴일 지정이 바뀌면 공휴일 구분이 갱신된다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val defaultContentColor = fixtureMonkey.giveMeColor()
        val holiday = LocalDate(year = 2026, month = Month.JULY, day = 13)
        var holidayProvider: () -> List<LocalDateRange> by mutableStateOf({ emptyList() })
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    Calendar(
                        state = rememberCalendarState(initialYearMonth = YearMonth(year = 2026, month = Month.JULY)),
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
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-009 주요 날짜가 아닌 날짜는 공휴일 일요일 토요일 그 밖의 날짜 순으로 구분된다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val defaultContentColor = fixtureMonkey.giveMeColor()
        val wednesdayHoliday = LocalDate(year = 2026, month = Month.JULY, day = 8)
        val saturdayHoliday = LocalDate(year = 2026, month = Month.JULY, day = 18)
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    Column {
                        listOf(1, 2).forEach { weekOfMonth ->
                            CalendarWeekOfMonth(
                                yearMonth = YearMonth(year = 2026, month = Month.JULY),
                                weekOfMonth = weekOfMonth,
                                modifier = Modifier.weight(1F),
                                holidayProvider = { listOf(wednesdayHoliday..wednesdayHoliday, saturdayHoliday..saturdayHoliday) },
                                colors = colors,
                            ) {}
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("5").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("8").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("18").textColor() shouldBe colors.sundayAndHolidayColor
        composeRule.onNodeWithText("11").textColor() shouldBe colors.saturdayColor
        composeRule.onNodeWithText("6").textColor() shouldBe defaultContentColor
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-FEATURE-015 지정한 달에 속하지 않는 날짜는 요일 구분을 유지한 채 지정한 달의 날짜와 구분된다`() {
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
