package io.github.taetae98coding.diary.feature.calendar.ui.home

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class CalendarHomeScaffoldStateTest :
    FunSpec({
        test("초기에는 날짜 선택 다이얼로그가 닫혀 있다") {
            val state =
                CalendarHomeScaffoldState(
                    calendarState = CalendarState(initialYearMonth = fixtureMonkey.giveMeYearMonth()),
                    datePickerDialogState = DialogState(),
                )

            state.isDatePickerVisible shouldBe false
        }

        test("초기에는 오늘 날짜가 정해져 있지 않다") {
            val state =
                CalendarHomeScaffoldState(
                    calendarState = CalendarState(initialYearMonth = fixtureMonkey.giveMeYearMonth()),
                    datePickerDialogState = DialogState(),
                )

            state.today shouldBe null
        }

        test("updateToday는 사용자의 현재 시간대 기준 오늘로 갱신한다") {
            val state =
                CalendarHomeScaffoldState(
                    calendarState = CalendarState(initialYearMonth = fixtureMonkey.giveMeYearMonth()),
                    datePickerDialogState = DialogState(),
                )

            state.updateToday()

            state.today shouldBe Clock.System.todayIn(TimeZone.currentSystemDefault())
        }

        test("showDatePicker는 현재 표시 중인 달의 1일로 날짜 선택 다이얼로그를 표시한다") {
            val yearMonth = fixtureMonkey.giveMeYearMonth()
            val state =
                CalendarHomeScaffoldState(
                    calendarState = CalendarState(initialYearMonth = yearMonth),
                    datePickerDialogState = DialogState(),
                )

            state.showDatePicker()

            state.isDatePickerVisible shouldBe true
            state.datePickerInitialDate shouldBe yearMonth.firstDay
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun FixtureMonkey.giveMeYearMonth(): YearMonth =
            YearMonth(
                year = giveMeOne<Int>().mod(9999) + 1,
                month = Month(giveMeOne<Int>().mod(12) + 1),
            )
    }
}
