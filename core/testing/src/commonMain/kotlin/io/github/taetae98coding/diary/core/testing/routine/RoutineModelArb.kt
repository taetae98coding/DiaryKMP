package io.github.taetae98coding.diary.core.testing.routine

import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus

private const val MAX_ROUTINE_AMOUNT = 9_999_999
private const val MAX_CYCLE_AMOUNT = 99_999_999L

public fun Arb.Companion.routineAmount(): Arb<Int> = Arb.int(1..MAX_ROUTINE_AMOUNT)

public fun Arb.Companion.routineDetail(): Arb<RoutineDetail> =
    arbitrary {
        RoutineDetail(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            startDate = Arb.kotlinxLocalDate().bind(),
            cycle = Arb.enum<RoutineCycle>().bind(),
            unit = Arb.routineUnit().bind(),
            goalAmount = Arb.routineAmount().orNull().bind(),
            amountPerRecord = Arb.routineAmount().bind(),
        )
    }

public fun Arb.Companion.routine(): Arb<Routine> =
    arbitrary {
        Routine(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.routineDetail().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.routineSummary(): Arb<RoutineSummary> =
    arbitrary {
        RoutineSummary(
            routine = Arb.routine().bind(),
            cycleAmount = Arb.long(0L..MAX_CYCLE_AMOUNT).bind(),
        )
    }

public fun Arb.Companion.routineRecord(): Arb<RoutineRecord> =
    arbitrary {
        RoutineRecord(
            id = Arb.kotlinUuid().bind(),
            routineId = Arb.kotlinUuid().bind(),
            date = Arb.kotlinxLocalDate().bind(),
            amount = Arb.routineAmount().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.calendarRoutineRecord(): Arb<CalendarRoutineRecord> =
    arbitrary {
        CalendarRoutineRecord(
            routineId = Arb.kotlinUuid().bind(),
            title = Arb.string().bind(),
            color = Arb.long().bind(),
            date = Arb.kotlinxLocalDate().bind(),
            amount = Arb.long(1L..MAX_CYCLE_AMOUNT).bind(),
            unit = Arb.routineUnit().bind(),
        )
    }

public fun Arb.Companion.routineCurrentCycle(): Arb<RoutineCurrentCycle> =
    arbitrary {
        val today = Arb.kotlinxLocalDate().bind()

        RoutineCurrentCycle(
            today = today,
            week = LocalDateRange(start = today, endInclusive = today),
            month = LocalDateRange(start = today, endInclusive = today),
        )
    }
