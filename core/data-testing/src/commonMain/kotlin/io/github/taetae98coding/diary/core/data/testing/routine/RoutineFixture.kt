package io.github.taetae98coding.diary.core.data.testing.routine

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public fun Arb.Companion.localRoutine(
    isDeleted: Boolean,
    isFinished: Boolean = false,
): Arb<RoutineLocalEntity> = Arb.routineLocalEntity().map { routine -> routine.copy(isFinished = isFinished, isDeleted = isDeleted) }

public fun Arb.Companion.remoteRoutine(
    isDeleted: Boolean,
    isFinished: Boolean = false,
): Arb<RoutineRemoteEntity> = Arb.routineRemoteEntity().map { routine -> routine.copy(isFinished = isFinished, isDeleted = isDeleted) }

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
