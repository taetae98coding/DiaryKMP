package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateRange
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarSelectDomainTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-001 이웃 달에 속한 날짜 칸도 실제 날짜 그대로 선택된다`() {
        val selectedList = mutableListOf<LocalDateRange>()
        composeRule.setCalendar(onSelect = { selectedList += it })
        composeRule.onAllNodesWithText("29").assertCountEquals(2)

        composeRule.performLongPress(composeRule.dayCenter(day = 29, index = 0))
        composeRule.performUp()

        selectedList shouldBe listOf(june(day = 29)..june(day = 29))
    }

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-003 날짜 선택을 사용하지 않는 화면에서는 길게 눌러도 아무 일도 일어나지 않는다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState, onSelect = null)

        composeRule.performLongPress(composeRule.dayCenter(day = 15))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        composeRule.performUp()

        calendarState.selectState.dateRange.shouldBeNull()
    }

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-004 가장자리 영역 밖에서는 선택 드래그로 달이 이동하지 않는다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        composeRule.performLongPress(composeRule.dayCenter(day = 15))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        composeRule.mainClock.advanceTimeBy(IDLE_WAIT_MILLIS)

        calendarState.currentYearMonth shouldBe JULY_2026
        composeRule.performUp()
    }

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-005 왼쪽 가장자리 영역에 머무르면 이전 달로 이동한다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        val start = composeRule.dayCenter(day = 15)
        composeRule.performLongPress(start)
        composeRule.mainClock.autoAdvance = false
        composeRule.performMove(Offset(x = 1F, y = start.y))

        // 6월 2026 화면에서 포인터 위치(셋째 주 일요일)의 날짜는 6월 14일이다.
        composeRule.advanceTimeUntil {
            calendarState.selectState.dateRange == june(day = 14)..july(day = 15)
        }

        calendarState.currentYearMonth shouldBe JUNE_2026
        composeRule.performUpAndSettle()
    }

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-006 이동 범위를 벗어나는 방향으로는 이동하지 않고 선택이 유지된다`() {
        val calendarState = CalendarState(initialYearMonth = FIRST_YEAR_MONTH)
        composeRule.setCalendar(calendarState = calendarState)

        val start = composeRule.dayCenter(day = 15)
        composeRule.performLongPress(start)
        composeRule.mainClock.autoAdvance = false
        composeRule.performMove(Offset(x = 1F, y = start.y))
        composeRule.mainClock.advanceTimeBy(IDLE_WAIT_MILLIS)

        calendarState.currentYearMonth shouldBe FIRST_YEAR_MONTH
        // 1년 1월 화면에서 포인터 위치(셋째 주 일요일)의 날짜는 1월 14일이다.
        calendarState.selectState.dateRange shouldBe firstYearJanuary(day = 14)..firstYearJanuary(day = 15)
        composeRule.performUpAndSettle()
    }

    @Test
    fun `가장자리 영역을 벗어나면 진행 중인 전환까지만 끝내고 달 이동 반복이 멈춘다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        val start = composeRule.dayCenter(day = 15)
        composeRule.performLongPress(start)
        composeRule.mainClock.autoAdvance = false
        composeRule.performMove(Offset(x = composeRule.rootWidth() - 1F, y = start.y))
        composeRule.advanceTimeUntil { calendarState.currentYearMonth == AUGUST_2026 }

        // 8월 도달 시점에 다음 전환이 이미 시작됐을 수 있으므로 9월까지는 허용하고, 그 뒤로는 이동하지 않아야 한다.
        composeRule.performMove(start)
        composeRule.mainClock.advanceTimeBy(IDLE_WAIT_MILLIS)
        val settledYearMonth = calendarState.currentYearMonth

        composeRule.mainClock.advanceTimeBy(IDLE_WAIT_MILLIS)

        (settledYearMonth == AUGUST_2026 || settledYearMonth == SEPTEMBER_2026) shouldBe true
        calendarState.currentYearMonth shouldBe settledYearMonth
        composeRule.performUpAndSettle()
    }
}
