package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.runtime.LaunchedEffect
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
class CalendarStateScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `animateScrollToPreviousMonth로 이전 달로 이동한다`() {
        val state = CalendarState(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))

        setCalendar(state) { it.animateScrollToPreviousMonth() }

        state.currentYearMonth shouldBe YearMonth(year = 2026, month = Month.JUNE)
    }

    @Test
    fun `animateScrollToNextMonth로 다음 달로 이동한다`() {
        val state = CalendarState(initialYearMonth = YearMonth(year = 2026, month = Month.JULY))

        setCalendar(state) { it.animateScrollToNextMonth() }

        state.currentYearMonth shouldBe YearMonth(year = 2026, month = Month.AUGUST)
    }

    @Test
    fun `1년 1월에서 animateScrollToPreviousMonth는 보던 달을 유지한다`() {
        val state = CalendarState(initialYearMonth = YearMonth(year = 1, month = Month.JANUARY))

        setCalendar(state) { it.animateScrollToPreviousMonth() }

        state.currentYearMonth shouldBe YearMonth(year = 1, month = Month.JANUARY)
    }

    private fun setCalendar(
        state: CalendarState,
        block: suspend (CalendarState) -> Unit,
    ) {
        composeRule.setContent {
            DiaryTheme {
                Calendar(state = state) {}
                LaunchedEffect(state) { block(state) }
            }
        }
        composeRule.waitForIdle()
    }
}
