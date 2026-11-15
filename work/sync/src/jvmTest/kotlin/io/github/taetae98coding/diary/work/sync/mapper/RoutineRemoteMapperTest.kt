package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineCycleRemoteEntity
import io.github.taetae98coding.diary.core.testing.routine.routineLocalEntity
import io.github.taetae98coding.diary.core.testing.routine.routineRecordLocalEntity
import io.github.taetae98coding.diary.core.testing.routine.routineRecordRemoteEntity
import io.github.taetae98coding.diary.core.testing.routine.routineRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

private val CYCLE_PAIR_LIST: List<Pair<RoutineCycleLocalEntity, RoutineCycleRemoteEntity>> =
    listOf(
        RoutineCycleLocalEntity.DAILY to RoutineCycleRemoteEntity.DAILY,
        RoutineCycleLocalEntity.WEEKDAYS to RoutineCycleRemoteEntity.WEEKDAYS,
        RoutineCycleLocalEntity.WEEKENDS to RoutineCycleRemoteEntity.WEEKENDS,
        RoutineCycleLocalEntity.WEEKLY to RoutineCycleRemoteEntity.WEEKLY,
        RoutineCycleLocalEntity.MONTHLY to RoutineCycleRemoteEntity.MONTHLY,
    )

class RoutineRemoteMapperTest :
    FunSpec({
        test("로컬 루틴은 원격 모델을 거쳐도 바뀌지 않는다") {
            val local = Arb.routineLocalEntity().next()

            local.toRemote().toLocal() shouldBe local
        }

        test("원격 루틴은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val remote = Arb.routineRemoteEntity().next()

            remote.toLocal().toRemote() shouldBe remote
        }

        test("로컬 루틴 기록은 원격 모델을 거쳐도 바뀌지 않는다") {
            val local = Arb.routineRecordLocalEntity().next()

            local.toRemote().toLocal() shouldBe local
        }

        test("원격 루틴 기록은 로컬 모델을 거쳐도 바뀌지 않는다") {
            val remote = Arb.routineRecordRemoteEntity().next()

            remote.toLocal().toRemote() shouldBe remote
        }

        test("TC-483afe78-01ae-44e6-b1e9-8deca12073b7 기기의 주기는 같은 의미의 원격 값으로 옮긴다") {
            CYCLE_PAIR_LIST.forEach { (local, remote) ->
                local.toRemote() shouldBe remote
            }
        }

        test("TC-d2c02683-b789-4a75-badf-29489a2c1cde 원격의 주기는 같은 의미의 기기 값으로 옮긴다") {
            CYCLE_PAIR_LIST.forEach { (local, remote) ->
                remote.toLocal() shouldBe local
            }
        }

        test("TC-06a7ffb0-3d16-48fd-a03f-423fae4bc4e3 기기의 단위는 적은 글자 그대로 원격 모델로 옮긴다") {
            listOf("회", "ml", "ML", "분 걷기", "kilometers", "").forEach { unit ->
                val local = Arb.routineLocalEntity().next().let { routine -> routine.copy(detail = routine.detail.copy(unit = unit)) }

                local.toRemote().detail.unit shouldBe unit
            }
        }

        test("TC-4534eddf-89a0-4740-a48a-fd3accd33b78 원격의 단위는 확인하지 않고 글자 그대로 기기 모델로 옮긴다") {
            listOf("회", "km", "ML", "분 걷기", "kilometers", "").forEach { unit ->
                val remote = Arb.routineRemoteEntity().next().let { routine -> routine.copy(detail = routine.detail.copy(unit = unit)) }

                remote.toLocal().detail.unit shouldBe unit
            }
        }
    })
