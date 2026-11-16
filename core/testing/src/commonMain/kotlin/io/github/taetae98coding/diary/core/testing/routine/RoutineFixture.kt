package io.github.taetae98coding.diary.core.testing.routine

import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
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
