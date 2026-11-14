package io.github.taetae98coding.diary.core.testing.routine

import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import kotlinx.datetime.LocalDateRange

private const val MAX_CYCLE_AMOUNT = 99_999_999L

public fun Arb.Companion.routineDetailLocalEntity(): Arb<RoutineDetailLocalEntity> =
    arbitrary {
        RoutineDetailLocalEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            startDate = Arb.kotlinxLocalDate().bind(),
            cycle = Arb.enum<RoutineCycleLocalEntity>().bind(),
            unit = Arb.routineUnit().bind(),
            goalAmount = Arb.routineAmount().orNull().bind(),
            amountPerRecord = Arb.routineAmount().bind(),
        )
    }

public fun Arb.Companion.routineLocalEntity(): Arb<RoutineLocalEntity> =
    arbitrary {
        RoutineLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.routineDetailLocalEntity().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.routineSummaryLocalEntity(): Arb<RoutineSummaryLocalEntity> =
    arbitrary {
        RoutineSummaryLocalEntity(
            routine = Arb.routineLocalEntity().bind(),
            cycleAmount = Arb.long(0L..MAX_CYCLE_AMOUNT).bind(),
        )
    }

public fun Arb.Companion.routineRecordLocalEntity(): Arb<RoutineRecordLocalEntity> =
    arbitrary {
        RoutineRecordLocalEntity(
            id = Arb.kotlinUuid().bind(),
            routineId = Arb.kotlinUuid().bind(),
            date = Arb.kotlinxLocalDate().bind(),
            amount = Arb.routineAmount().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.calendarRoutineRecordLocalEntity(): Arb<CalendarRoutineRecordLocalEntity> =
    arbitrary {
        CalendarRoutineRecordLocalEntity(
            routineId = Arb.kotlinUuid().bind(),
            title = Arb.string().bind(),
            color = Arb.long().bind(),
            date = Arb.kotlinxLocalDate().bind(),
            amount = Arb.long(1L..MAX_CYCLE_AMOUNT).bind(),
            unit = Arb.routineUnit().bind(),
        )
    }

public fun Arb.Companion.routineCurrentCycleLocalEntity(): Arb<RoutineCurrentCycleLocalEntity> =
    arbitrary {
        val today = Arb.kotlinxLocalDate().bind()

        RoutineCurrentCycleLocalEntity(
            today = today,
            week = LocalDateRange(start = today, endInclusive = today),
            month = LocalDateRange(start = today, endInclusive = today),
        )
    }
