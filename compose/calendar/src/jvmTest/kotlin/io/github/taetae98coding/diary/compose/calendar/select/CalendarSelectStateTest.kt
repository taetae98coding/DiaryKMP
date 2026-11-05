package io.github.taetae98coding.diary.compose.calendar.select

import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.plus

class CalendarSelectStateTest :
    FunSpec({
        test("TC-b53b60c5-75a4-4a0e-8adf-e62369a5ee19 선택을 해제하면 선택된 기간이 없는 상태가 된다") {
            val state = CalendarSelectState()
            state.select(Arb.localDateRange().next())

            state.clear()

            state.dateRange.shouldBeNull()
        }

        test("선택 전에는 선택된 기간이 없다") {
            val state = CalendarSelectState()

            state.dateRange.shouldBeNull()
        }

        test("선택하면 전달된 기간이 그대로 저장된다") {
            val state = CalendarSelectState()
            val dateRange = Arb.localDateRange().next()

            state.select(dateRange)

            state.dateRange shouldBe dateRange
        }

        test("이어서 선택하면 마지막 기간으로 갱신된다") {
            val state = CalendarSelectState()
            val first = Arb.localDateRange().next()
            val second = Arb.localDateRange().next()

            state.select(first)
            state.select(second)

            state.dateRange shouldBe second
        }
    })
