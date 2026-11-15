package io.github.taetae98coding.diary.core.testing.routine

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public fun Arb.Companion.routine(
    isDeleted: Boolean,
    detail: RoutineDetail? = null,
    isFinished: Boolean = false,
): Arb<Routine> =
    arbitrary {
        Arb.routine().bind().copy(
            detail = detail ?: Arb.routineDetail().bind(),
            isFinished = isFinished,
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.localRoutine(
    isDeleted: Boolean,
    isFinished: Boolean = false,
): Arb<RoutineLocalEntity> = Arb.routineLocalEntity().map { routine -> routine.copy(isFinished = isFinished, isDeleted = isDeleted) }

public fun Arb.Companion.remoteRoutine(
    isDeleted: Boolean,
    isFinished: Boolean = false,
): Arb<RoutineRemoteEntity> = Arb.routineRemoteEntity().map { routine -> routine.copy(isFinished = isFinished, isDeleted = isDeleted) }

// 진행 상태, 이번 주기와 달성률을 정하는 값만 고정하고 나머지는 임의로 채운다. 목표량을 넘기지 않으면 목표량이 없는 루틴이다.
public fun Arb.Companion.routineDetail(
    startDate: LocalDate,
    cycle: RoutineCycle = RoutineCycle.DAILY,
    goalAmount: Int? = null,
): Arb<RoutineDetail> =
    arbitrary {
        Arb.routineDetail().bind().copy(
            startDate = startDate,
            cycle = cycle,
            goalAmount = goalAmount,
        )
    }

public fun Arb.Companion.localRoutine(
    startDate: LocalDate,
    cycle: RoutineCycleLocalEntity = RoutineCycleLocalEntity.DAILY,
    title: String? = null,
    isFinished: Boolean = false,
    isDeleted: Boolean = false,
    unit: String? = null,
    goalAmount: Int? = null,
): Arb<RoutineLocalEntity> =
    arbitrary {
        val routine = Arb.routineLocalEntity().bind()

        routine.copy(
            detail =
                routine.detail.copy(
                    title = title ?: routine.detail.title,
                    startDate = startDate,
                    cycle = cycle,
                    unit = unit ?: routine.detail.unit,
                    goalAmount = goalAmount,
                ),
            isFinished = isFinished,
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.routineRecord(
    routineId: Uuid,
    isDeleted: Boolean = false,
): Arb<RoutineRecord> =
    Arb.routineRecord().map { record ->
        record.copy(
            routineId = routineId,
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.localRoutineRecord(
    routineId: Uuid,
    date: LocalDate? = null,
    amount: Int? = null,
    isDeleted: Boolean = false,
): Arb<RoutineRecordLocalEntity> =
    Arb.routineRecordLocalEntity().map { record ->
        record.copy(
            routineId = routineId,
            date = date ?: record.date,
            amount = amount ?: record.amount,
            isDeleted = isDeleted,
        )
    }

public fun Arb.Companion.remoteRoutineRecord(
    routineId: Uuid,
    isDeleted: Boolean = false,
): Arb<RoutineRecordRemoteEntity> =
    Arb.routineRecordRemoteEntity().map { record ->
        record.copy(
            routineId = routineId,
            isDeleted = isDeleted,
        )
    }
