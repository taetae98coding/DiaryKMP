package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

class RoutineHomeFetchDateRangeTest :
    FunSpec({
        test("TC-be5be321-fffd-42b4-9da5-ab07615d259b 조회 기간은 표시 중인 달의 두 달 전 1일부터 두 달 후 말일까지다") {
            YearMonth(year = 2026, month = Month.AUGUST).routineHomeFetchDateRange() shouldBe
                LocalDate(year = 2026, month = Month.JUNE, day = 1)..LocalDate(year = 2026, month = Month.OCTOBER, day = 31)
        }

        test("조회 기간은 연도와 윤년의 2월 말일을 넘나든다") {
            YearMonth(year = 2027, month = Month.DECEMBER).routineHomeFetchDateRange() shouldBe
                LocalDate(year = 2027, month = Month.OCTOBER, day = 1)..LocalDate(year = 2028, month = Month.FEBRUARY, day = 29)
            YearMonth(year = 2026, month = Month.JANUARY).routineHomeFetchDateRange() shouldBe
                LocalDate(year = 2025, month = Month.NOVEMBER, day = 1)..LocalDate(year = 2026, month = Month.MARCH, day = 31)
        }
    })
