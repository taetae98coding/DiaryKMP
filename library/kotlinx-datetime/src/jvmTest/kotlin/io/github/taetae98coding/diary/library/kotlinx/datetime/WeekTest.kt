package io.github.taetae98coding.diary.library.kotlinx.datetime

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

class WeekTest : FunSpec() {
    init {
        test("요일을 일요일 0부터 토요일 6까지로 센다") {
            DayOfWeek.SUNDAY.sundayBasedNumber shouldBe 0
            DayOfWeek.MONDAY.sundayBasedNumber shouldBe 1
            DayOfWeek.SATURDAY.sundayBasedNumber shouldBe 6
        }

        test("일요일 기준 번호를 요일로 되돌린다") {
            sundayBasedDayOfWeek(number = 0) shouldBe DayOfWeek.SUNDAY
            sundayBasedDayOfWeek(number = 1) shouldBe DayOfWeek.MONDAY
            sundayBasedDayOfWeek(number = 6) shouldBe DayOfWeek.SATURDAY
        }

        test("요일과 일요일 기준 번호는 서로 되돌릴 수 있다") {
            DayOfWeek.entries.forEach { dayOfWeek ->
                sundayBasedDayOfWeek(number = dayOfWeek.sundayBasedNumber) shouldBe dayOfWeek
            }
        }

        test("일요일 기준 번호는 일요일부터 토요일까지 한 주를 모두 담는다") {
            (0..<DAYS_PER_WEEK).map { number -> sundayBasedDayOfWeek(number = number) } shouldBe
                listOf(
                    DayOfWeek.SUNDAY,
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                )
        }

        test("일요일 기준 번호는 한 주를 벗어나지 않는다") {
            DayOfWeek.entries.map { dayOfWeek -> dayOfWeek.sundayBasedNumber }.toSet() shouldBe (0..<DAYS_PER_WEEK).toSet()
        }

        test("날짜가 속한 주의 일요일을 구한다") {
            // 2026년 7월 12일은 일요일이다.
            val sunday = LocalDate(year = 2026, month = Month.JULY, day = 12)

            sunday.sundayOfWeek() shouldBe sunday
            LocalDate(year = 2026, month = Month.JULY, day = 13).sundayOfWeek() shouldBe sunday
            LocalDate(year = 2026, month = Month.JULY, day = 18).sundayOfWeek() shouldBe sunday
        }

        test("달이 바뀌어도 날짜가 속한 주의 일요일을 구한다") {
            // 2026년 8월 1일은 토요일이고, 그 주의 일요일은 7월 26일이다.
            LocalDate(year = 2026, month = Month.AUGUST, day = 1).sundayOfWeek() shouldBe
                LocalDate(year = 2026, month = Month.JULY, day = 26)
        }
    }
}
