package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateRange
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarSelectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-001 날짜 칸을 길게 누르면 그 날짜 하루가 선택된다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        composeRule.performLongPress(composeRule.dayCenter(day = 15))

        calendarState.selectState.dateRange shouldBe july(day = 15)..july(day = 15)
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-002 길게 누른 채 뒤 날짜로 드래그하면 처음 날짜부터 가리키는 날짜까지 선택된다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        composeRule.performUp()

        calendarState.selectState.dateRange shouldBe july(day = 14)..july(day = 17)
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-003 처음 날짜보다 이전 날짜로 드래그하면 가리키는 날짜부터 처음 날짜까지 선택된다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        composeRule.performLongPress(composeRule.dayCenter(day = 17))
        composeRule.performMoveTo(composeRule.dayCenter(day = 14))
        composeRule.performUp()

        calendarState.selectState.dateRange shouldBe july(day = 14)..july(day = 17)
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-004 주 경계를 넘어 드래그하면 여러 주에 걸친 기간이 선택된다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        composeRule.performLongPress(composeRule.dayCenter(day = 15))
        composeRule.performMoveTo(composeRule.dayCenter(day = 22))
        composeRule.performUp()

        calendarState.selectState.dateRange shouldBe july(day = 15)..july(day = 22)
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-005 드래그하는 동안 가리키는 날짜가 바뀌면 선택이 즉시 갱신된다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        calendarState.selectState.dateRange shouldBe july(day = 14)..july(day = 17)

        composeRule.performMoveTo(composeRule.dayCenter(day = 15))

        calendarState.selectState.dateRange shouldBe july(day = 14)..july(day = 15)
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-006 손을 떼면 그 시점의 기간으로 선택이 완료된다`() {
        val selectedList = mutableListOf<LocalDateRange>()
        composeRule.setCalendar(onSelect = { selectedList += it })

        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        composeRule.performUp()

        selectedList shouldBe listOf(july(day = 14)..july(day = 17))
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-007 드래그 없이 길게 누르기만 하고 손을 떼면 하루짜리 기간으로 완료된다`() {
        val selectedList = mutableListOf<LocalDateRange>()
        composeRule.setCalendar(onSelect = { selectedList += it })

        composeRule.performLongPress(composeRule.dayCenter(day = 15))
        composeRule.performUp()

        selectedList shouldBe listOf(july(day = 15)..july(day = 15))
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-009 선택 중 오른쪽 가장자리 영역에 머무르면 다음 달로 이동하고 선택이 유지된다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        val start = composeRule.dayCenter(day = 15)
        composeRule.performLongPress(start)
        composeRule.mainClock.autoAdvance = false
        composeRule.performMove(Offset(x = composeRule.rootWidth() - 1F, y = start.y))

        // 8월 2026 화면에서 포인터 위치(셋째 주 토요일)의 날짜는 8월 15일이다.
        composeRule.advanceTimeUntil {
            calendarState.selectState.dateRange == july(day = 15)..august(day = 15)
        }

        calendarState.currentYearMonth shouldBe AUGUST_2026
        composeRule.performUpAndSettle()
    }

    @Test
    fun `TC-CALENDAR-SELECT-FEATURE-010 가장자리 영역에 머무는 동안 달 이동이 반복된다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        composeRule.setCalendar(calendarState = calendarState)

        val start = composeRule.dayCenter(day = 15)
        composeRule.performLongPress(start)
        composeRule.mainClock.autoAdvance = false
        composeRule.performMove(Offset(x = composeRule.rootWidth() - 1F, y = start.y))
        composeRule.advanceTimeUntil { calendarState.currentYearMonth == AUGUST_2026 }

        // 9월 2026 화면에서 포인터 위치(셋째 주 토요일)의 날짜는 9월 19일이다.
        composeRule.advanceTimeUntil {
            calendarState.selectState.dateRange == july(day = 15)..september(day = 19)
        }

        calendarState.currentYearMonth shouldBe SEPTEMBER_2026
        composeRule.performUpAndSettle()
    }
}
