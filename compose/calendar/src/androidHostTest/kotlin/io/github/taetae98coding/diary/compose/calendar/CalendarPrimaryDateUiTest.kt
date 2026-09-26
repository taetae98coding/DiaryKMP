package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.week.CalendarWeekOfMonth
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
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

    private var onPrimary = Color.Unspecified

    @Test
    fun `TC-CALENDAR-FEATURE-017 지정한 주요 날짜가 강조된다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        setCalendar(defaultContentColor = defaultContentColor) { listOf(july(day = 15)) }

        composeRule.onNodeWithText("15").textColor() shouldBe onPrimary
        composeRule.onNodeWithText("14").textColor() shouldBe defaultContentColor
    }

    @Test
    fun `TC-CALENDAR-FEATURE-018 주요 날짜를 지정하지 않으면 어떤 날짜도 강조되지 않는다`() {
        setCalendar(defaultContentColor = fixtureMonkey.giveMeColor()) { emptyList() }

        dayTextColors().apply {
            shouldHaveSize(DAYS_PER_CALENDAR)
            shouldNotContain(onPrimary)
        }
    }

    @Test
    fun `TC-CALENDAR-DOMAIN-001 주요 날짜 지정이 바뀌면 강조가 갱신된다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        var primaryDateList by mutableStateOf(emptyList<LocalDate>())
        setCalendar(defaultContentColor = defaultContentColor) { primaryDateList }

        composeRule.onNodeWithText("15").textColor() shouldBe defaultContentColor

        composeRule.runOnIdle { primaryDateList = listOf(july(day = 15)) }
        composeRule.onNodeWithText("15").textColor() shouldBe onPrimary

        composeRule.runOnIdle { primaryDateList = emptyList() }
        composeRule.onNodeWithText("15").textColor() shouldBe defaultContentColor
    }

    @Test
    fun `TC-CALENDAR-DOMAIN-002 같은 주요 날짜를 여러 번 지정해도 한 번 지정한 것과 같다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        setCalendar(defaultContentColor = defaultContentColor) { List(DUPLICATE_COUNT) { july(day = 15) } }

        composeRule.onNodeWithText("15").textColor() shouldBe onPrimary
        dayTextColors().count { color -> color == onPrimary } shouldBe 1
    }

    @Test
    fun `TC-CALENDAR-DOMAIN-003 표시 중인 달에 없는 날짜를 주요 날짜로 지정해도 강조 대상이 없는 것으로 처리한다`() {
        setCalendar(defaultContentColor = fixtureMonkey.giveMeColor()) {
            listOf(LocalDate(year = 2026, month = Month.DECEMBER, day = 25))
        }

        dayTextColors().apply {
            shouldHaveSize(DAYS_PER_CALENDAR)
            shouldNotContain(onPrimary)
        }
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-008 주요 날짜는 요일과 공휴일 여부에 관계없이 주요 날짜로 구분된다`() {
        val colors = fixtureMonkey.giveMeCalendarColor()
        val sunday = july(day = 12)
        val holiday = july(day = 13)
        val saturday = july(day = 18)

        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CalendarWeekOfMonth(
                    yearMonth = JULY_2026,
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
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-010 이웃 달에 속한 주요 날짜는 이웃 달 구분보다 주요 날짜 강조를 우선한다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        val primaryDate = LocalDate(year = 2026, month = Month.JUNE, day = 28)

        setCalendarWeekOfMonth(
            yearMonth = JULY_2026,
            weekOfMonth = 0,
            defaultContentColor = defaultContentColor,
            primaryDateList = listOf(primaryDate),
        )

        composeRule.onNodeWithText("28").textColor() shouldBe onPrimary
        composeRule.onNodeWithText("29").textColor() shouldBe defaultContentColor.adjacentMonth()
    }

    @Test
    fun `TC-CALENDAR-WEEK-OF-MONTH-DOMAIN-011 주 전체가 지정한 달 밖이면 모든 날짜가 이웃 달로 구분되고 주요 날짜만 강조를 유지한다`() {
        val defaultContentColor = fixtureMonkey.giveMeColor()
        val colors = fixtureMonkey.giveMeCalendarColor()

        setCalendarWeekOfMonth(
            yearMonth = YearMonth(year = 2026, month = Month.FEBRUARY),
            weekOfMonth = 5,
            defaultContentColor = defaultContentColor,
            primaryDateList = listOf(LocalDate(year = 2026, month = Month.MARCH, day = 10)),
            colors = colors,
        )

        composeRule.onNodeWithText("10").textColor() shouldBe onPrimary
        composeRule.onNodeWithText("8").textColor() shouldBe colors.sundayAndHolidayColor.adjacentMonth()
        listOf("9", "11", "12", "13").forEach { day ->
            composeRule.onNodeWithText(day).textColor() shouldBe defaultContentColor.adjacentMonth()
        }
        composeRule.onNodeWithText("14").textColor() shouldBe colors.saturdayColor.adjacentMonth()
    }

    private fun setCalendar(
        defaultContentColor: Color,
        primaryDateProvider: () -> List<LocalDate>,
    ) {
        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    Calendar(
                        state = rememberCalendarState(initialYearMonth = JULY_2026),
                        primaryDateProvider = primaryDateProvider,
                    ) {}
                }
            }
        }
    }

    private fun setCalendarWeekOfMonth(
        yearMonth: YearMonth,
        weekOfMonth: Int,
        defaultContentColor: Color,
        primaryDateList: List<LocalDate>,
        colors: CalendarColor? = null,
    ) {
        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary

                CompositionLocalProvider(LocalContentColor provides defaultContentColor) {
                    CalendarWeekOfMonth(
                        yearMonth = yearMonth,
                        weekOfMonth = weekOfMonth,
                        primaryDateProvider = { primaryDateList },
                        colors = colors ?: CalendarDefault.colors(),
                    ) {}
                }
            }
        }
    }

    private fun dayTextColors(): List<Color> {
        val nodes = composeRule.onAllNodes(hasText(text = "", substring = true))

        return nodes
            .fetchSemanticsNodes()
            .mapIndexedNotNull { index, node ->
                val text = node.config[SemanticsProperties.Text].joinToString(separator = "") { it.text }

                if (text.isNotEmpty() && text.all(Char::isDigit)) nodes[index].textColor() else null
            }
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

    private fun Color.adjacentMonth(): Color = copy(alpha = alpha * ADJACENT_MONTH_DAY_ALPHA)

    companion object {
        private const val ADJACENT_MONTH_DAY_ALPHA = 0.38F
        private const val DAYS_PER_CALENDAR = 42
        private const val DUPLICATE_COUNT = 3
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}

private fun FixtureMonkey.giveMeCalendarColor(): CalendarColor =
    CalendarColor(
        sundayAndHolidayColor = giveMeColor(),
        saturdayColor = giveMeColor(),
    )

private fun FixtureMonkey.giveMeColor(): Color = Color(color = giveMeOne<Int>())
