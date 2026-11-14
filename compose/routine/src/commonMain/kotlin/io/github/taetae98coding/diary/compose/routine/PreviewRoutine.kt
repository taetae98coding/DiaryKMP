package io.github.taetae98coding.diary.compose.routine

import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun previewRoutineToday(): LocalDate = LocalDate(year = 2026, month = 7, day = 19)

public fun previewRoutineDetail(
    title: String,
    startDate: LocalDate = LocalDate(year = 2026, month = 1, day = 1),
    cycle: RoutineCycle = RoutineCycle.DAILY,
    unit: String = "ml",
    goalAmount: Int? = 2000,
    amountPerRecord: Int = 250,
    color: Long = 0xFF3A7BD5,
): RoutineDetail =
    RoutineDetail(
        title = title,
        description = "",
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

public fun previewRoutineSummary(
    title: String,
    cycleAmount: Long = 1500,
    detail: RoutineDetail = previewRoutineDetail(title = title),
    isFinished: Boolean = false,
): RoutineSummary =
    RoutineSummary(
        routine =
            Routine(
                id = Uuid.random(),
                detail = detail,
                isFinished = isFinished,
                isDeleted = false,
                updatedAt = Instant.DISTANT_PAST,
                createdAt = Instant.DISTANT_PAST,
            ),
        cycleAmount = cycleAmount,
    )
