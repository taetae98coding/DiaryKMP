package io.github.taetae98coding.diary.compose.calendar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

class CalendarStateTest :
    FunSpec({
        test("0페이지는 1년 1월이다") {
            0.toYearMonth() shouldBe YearMonth(year = 1, month = Month.JANUARY)
        }

        test("페이지와 달의 매핑이 왕복 보존된다") {
            val page = Arb.int().next().mod(Int.MAX_VALUE)

            page.toYearMonth().toPage() shouldBe page
        }

        test("currentYearMonth는 초기 달을 반영한다") {
            val initialYearMonth =
                Arb
                    .int()
                    .next()
                    .mod(Int.MAX_VALUE)
                    .toYearMonth()

            val state = CalendarState(initialYearMonth = initialYearMonth)

            state.currentYearMonth shouldBe initialYearMonth
        }
    })
