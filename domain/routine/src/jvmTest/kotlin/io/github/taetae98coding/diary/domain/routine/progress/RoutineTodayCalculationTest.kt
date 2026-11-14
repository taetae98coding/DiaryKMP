package io.github.taetae98coding.diary.domain.routine.progress

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek

class RoutineTodayCalculationTest :
    FunSpec({
        test("주기마다 하는 요일은 요일 일곱 개를 빠짐없이 나눈다") {
            val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

            DayOfWeek.entries.forEach { dayOfWeek ->
                RoutineCycle.DAILY.isDoneOn(dayOfWeek = dayOfWeek) shouldBe true
                RoutineCycle.WEEKLY.isDoneOn(dayOfWeek = dayOfWeek) shouldBe true
                RoutineCycle.MONTHLY.isDoneOn(dayOfWeek = dayOfWeek) shouldBe true
                RoutineCycle.WEEKENDS.isDoneOn(dayOfWeek = dayOfWeek) shouldBe (dayOfWeek in weekend)
                RoutineCycle.WEEKDAYS.isDoneOn(dayOfWeek = dayOfWeek) shouldBe (dayOfWeek !in weekend)
            }
        }
    })
