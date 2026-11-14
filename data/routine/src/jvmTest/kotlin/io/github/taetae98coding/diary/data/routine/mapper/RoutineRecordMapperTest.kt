package io.github.taetae98coding.diary.data.routine.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.testing.routine.calendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.core.testing.routine.routineRecordLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class RoutineRecordMapperTest :
    FunSpec({
        test("루틴 기록은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val record = Arb.routineRecord().next()

            record.toLocal().toDomain() shouldBe record
        }

        test("로컬 루틴 기록은 도메인 모델을 거쳐도 바뀌지 않는다") {
            val local = Arb.routineRecordLocalEntity().next()

            local.toDomain().toLocal() shouldBe local
        }

        test("TC-02e860c4-c7b1-43ad-aff5-abfd981cd4e7 캘린더 루틴 기록은 식별값, 제목, 컬러, 날짜, 합계와 단위를 옮긴다") {
            mapOf(
                "count" to "count",
                "ml" to "ml",
                "m" to "m",
            ).forEach { (localUnit, unit) ->
                val local = Arb.calendarRoutineRecordLocalEntity().next().copy(unit = localUnit)

                local.toDomain() shouldBe
                    CalendarRoutineRecord(
                        routineId = local.routineId,
                        title = local.title,
                        color = local.color,
                        date = local.date,
                        amount = local.amount,
                        unit = unit,
                    )
            }
        }

        test("캘린더 루틴 기록의 합계는 양이 가장 큰 기록 둘을 더한 값도 그대로 옮긴다") {
            val local: CalendarRoutineRecordLocalEntity = Arb.calendarRoutineRecordLocalEntity().next().copy(amount = 19_999_998L)

            local.toDomain().amount shouldBe 19_999_998L
        }
    })
