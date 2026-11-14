package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineCurrentCycle
import io.github.taetae98coding.diary.core.testing.routine.routineLocalEntity
import io.github.taetae98coding.diary.core.testing.routine.routineSummaryLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class RoutineMapperTest :
    FunSpec({
        test("루틴은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val routine = Arb.routine().next()

            routine.toLocal().toDomain() shouldBe routine
        }

        test("완료 여부는 로컬 모델과 도메인 모델 사이에서 그대로 옮긴다") {
            listOf(true, false).forEach { isFinished ->
                val routine = Arb.routine().next().copy(isFinished = isFinished)

                routine.toLocal().isFinished shouldBe isFinished
                routine.toLocal().toDomain().isFinished shouldBe isFinished
            }
        }

        test("로컬 루틴은 도메인 모델을 거쳐도 바뀌지 않는다") {
            val local = Arb.routineLocalEntity().next()

            local.toDomain().toLocal() shouldBe local
        }

        test("TC-6f442d59-63b6-4f58-b781-7060329ecdce 주기는 같은 의미의 로컬 값과 짝을 이룬다") {
            mapOf(
                RoutineCycle.DAILY to RoutineCycleLocalEntity.DAILY,
                RoutineCycle.WEEKDAYS to RoutineCycleLocalEntity.WEEKDAYS,
                RoutineCycle.WEEKENDS to RoutineCycleLocalEntity.WEEKENDS,
                RoutineCycle.WEEKLY to RoutineCycleLocalEntity.WEEKLY,
                RoutineCycle.MONTHLY to RoutineCycleLocalEntity.MONTHLY,
            ).forEach { (cycle, local) ->
                cycle.toLocal() shouldBe local
                local.toDomain() shouldBe cycle
            }
        }

        test("주기는 다섯 값이 빠짐없이 로컬 값과 짝을 이룬다") {
            RoutineCycle.entries.map { cycle -> cycle.toLocal() }.toSet() shouldBe RoutineCycleLocalEntity.entries.toSet()
            RoutineCycleLocalEntity.entries.map { local -> local.toDomain() }.toSet() shouldBe RoutineCycle.entries.toSet()
        }

        test("단위는 로컬 모델과 도메인 모델 사이에서 적은 글자 그대로 옮긴다") {
            listOf("회", "ML", "분 걷기", "kilometers", "").forEach { unit ->
                val routine = Arb.routine().next().let { routine -> routine.copy(detail = routine.detail.copy(unit = unit)) }

                routine.toLocal().detail.unit shouldBe unit
                routine
                    .toLocal()
                    .toDomain()
                    .detail.unit shouldBe unit
            }
        }

        test("루틴 요약은 루틴과 주기의 합계를 그대로 옮긴다") {
            val local = Arb.routineSummaryLocalEntity().next()

            local.toDomain() shouldBe RoutineSummary(routine = local.routine.toDomain(), cycleAmount = local.cycleAmount)
        }

        test("이번 주기는 오늘, 주와 달을 그대로 옮긴다") {
            val currentCycle = Arb.routineCurrentCycle().next()

            currentCycle.toLocal() shouldBe RoutineCurrentCycleLocalEntity(today = currentCycle.today, week = currentCycle.week, month = currentCycle.month)
        }
    })
