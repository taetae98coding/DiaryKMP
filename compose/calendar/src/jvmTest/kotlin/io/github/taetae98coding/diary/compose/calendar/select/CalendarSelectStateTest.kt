package io.github.taetae98coding.diary.compose.calendar.select

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus

class CalendarSelectStateTest :
    FunSpec({
        test("TC-b53b60c5-75a4-4a0e-8adf-e62369a5ee19 선택을 해제하면 선택된 기간이 없는 상태가 된다") {
            val state = CalendarSelectState()
            state.select(randomDateRange())

            state.clear()

            state.dateRange.shouldBeNull()
        }

        test("선택 전에는 선택된 기간이 없다") {
            val state = CalendarSelectState()

            state.dateRange.shouldBeNull()
        }

        test("선택하면 전달된 기간이 그대로 저장된다") {
            val state = CalendarSelectState()
            val dateRange = randomDateRange()

            state.select(dateRange)

            state.dateRange shouldBe dateRange
        }

        test("이어서 선택하면 마지막 기간으로 갱신된다") {
            val state = CalendarSelectState()
            val first = randomDateRange()
            val second = randomDateRange()

            state.select(first)
            state.select(second)

            state.dateRange shouldBe second
        }
    }) {
    public companion object {
        private fun randomDateRange(): LocalDateRange {
            val start = LocalDate.fromEpochDays(Arb.int().next().mod(1_000_000))

            return start..start.plus(Arb.int().next().mod(30), DateTimeUnit.DAY)
        }
    }
}
