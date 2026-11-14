package io.github.taetae98coding.diary.domain.routine.progress

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

class RoutineCurrentCycleCalculationTest :
    FunSpec({
        test("TC-afa98e83-e823-4df3-8263-f7012e5a5568 매일 루틴의 이번 주기는 오늘 하루다") {
            val today = LocalDate(2026, 11, 13)

            routineCurrentCycle(today = today).range(cycle = RoutineCycle.DAILY) shouldBe today..today
        }

        test("TC-731d6bde-1311-45ee-80b1-7605c8a6708f 평일만과 주말만 루틴의 이번 주기는 하는 요일이 아닌 날에도 오늘 하루다") {
            listOf(
                RoutineCycle.WEEKDAYS to LocalDate(2026, 11, 13),
                RoutineCycle.WEEKDAYS to LocalDate(2026, 11, 14),
                RoutineCycle.WEEKDAYS to LocalDate(2026, 11, 15),
                RoutineCycle.WEEKENDS to LocalDate(2026, 11, 14),
                RoutineCycle.WEEKENDS to LocalDate(2026, 11, 13),
                RoutineCycle.WEEKENDS to LocalDate(2026, 11, 16),
            ).forEach { (cycle, today) ->
                routineCurrentCycle(today = today).range(cycle = cycle) shouldBe today..today
            }
        }

        test("TC-31e9ea3d-a0b6-46da-b4f9-b8d5401e116e 매주 루틴의 이번 주기는 오늘이 속한 주의 일요일부터 토요일까지다") {
            listOf(
                LocalDate(2026, 11, 8) to LocalDate(2026, 11, 8)..LocalDate(2026, 11, 14),
                LocalDate(2026, 11, 11) to LocalDate(2026, 11, 8)..LocalDate(2026, 11, 14),
                LocalDate(2026, 11, 14) to LocalDate(2026, 11, 8)..LocalDate(2026, 11, 14),
                LocalDate(2026, 11, 15) to LocalDate(2026, 11, 15)..LocalDate(2026, 11, 21),
                LocalDate(2026, 12, 1) to LocalDate(2026, 11, 29)..LocalDate(2026, 12, 5),
                LocalDate(2027, 1, 1) to LocalDate(2026, 12, 27)..LocalDate(2027, 1, 2),
            ).forEach { (today, expected) ->
                routineCurrentCycle(today = today).range(cycle = RoutineCycle.WEEKLY) shouldBe expected
            }
        }

        test("TC-423bd999-9c1a-4660-b88d-e7bf1a7ceb07 매월 루틴의 이번 주기는 오늘이 속한 달의 1일부터 마지막 날까지다") {
            listOf(
                LocalDate(2026, 11, 1) to LocalDate(2026, 11, 1)..LocalDate(2026, 11, 30),
                LocalDate(2026, 11, 30) to LocalDate(2026, 11, 1)..LocalDate(2026, 11, 30),
                LocalDate(2026, 12, 31) to LocalDate(2026, 12, 1)..LocalDate(2026, 12, 31),
                LocalDate(2026, 2, 28) to LocalDate(2026, 2, 1)..LocalDate(2026, 2, 28),
                LocalDate(2028, 2, 1) to LocalDate(2028, 2, 1)..LocalDate(2028, 2, 29),
                LocalDate(2028, 2, 29) to LocalDate(2028, 2, 1)..LocalDate(2028, 2, 29),
                LocalDate(2028, 3, 1) to LocalDate(2028, 3, 1)..LocalDate(2028, 3, 31),
            ).forEach { (today, expected) ->
                routineCurrentCycle(today = today).range(cycle = RoutineCycle.MONTHLY) shouldBe expected
            }
        }

        test("TC-38129340-a109-4d36-8eee-da47c51dd29a 이번 주기는 루틴의 시작 날짜로 좁히지 않는다") {
            // 이번 주기는 오늘과 주기만 입력으로 받으므로 시작 날짜 2026-11-11이나 2026-11-10이 주기 안에 있어도 범위가 같다.
            routineCurrentCycle(today = LocalDate(2026, 11, 11)).range(cycle = RoutineCycle.WEEKLY) shouldBe
                LocalDate(2026, 11, 8)..LocalDate(2026, 11, 14)
            routineCurrentCycle(today = LocalDate(2026, 11, 13)).range(cycle = RoutineCycle.MONTHLY) shouldBe
                LocalDate(2026, 11, 1)..LocalDate(2026, 11, 30)
        }

        test("이번 주기는 오늘을 그대로 담는다") {
            val today = LocalDate(2026, 11, 13)

            routineCurrentCycle(today = today).today shouldBe today
        }
    })
