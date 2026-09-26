package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class CalendarTimetableFetchDateRangeTest :
    FunSpec({
        test("TC-CALENDAR-TIMETABLE-DATA-003 조회 기간은 표시 날짜와 앞뒤로 한 번 넘겼을 때 보이는 날짜를 포함한다") {
            (date(month = 9, day = 23)..date(month = 9, day = 23)).calendarTimetableFetchDateRange() shouldBe
                date(month = 9, day = 22)..date(month = 9, day = 24)
            (date(month = 9, day = 20)..date(month = 9, day = 26)).calendarTimetableFetchDateRange() shouldBe
                date(month = 9, day = 13)..date(month = 10, day = 3)
        }
    })

private fun date(
    month: Int,
    day: Int,
): LocalDate = LocalDate(year = 2026, month = month, day = day)
