package io.github.taetae98coding.diary.domain.routine

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineUnitInvalidException
import kotlinx.datetime.LocalDate

public data class RoutineInput(
    val title: String,
    val description: String,
    val color: Long,
    val startDate: LocalDate,
    val cycle: RoutineCycle,
    val unit: String,
    val goalAmount: Int?,
    val amountPerRecord: Int,
)

internal fun RoutineInput.toDetail(): RoutineDetail {
    val unit = unit.trim()

    invalidException(unit = unit)?.let { exception -> throw exception }

    return RoutineDetail(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )
}

private fun RoutineInput.invalidException(unit: String): Exception? =
    when {
        goalAmount != null && goalAmount !in ROUTINE_AMOUNT_RANGE -> RoutineGoalAmountInvalidException()
        amountPerRecord !in ROUTINE_AMOUNT_RANGE -> RoutineAmountPerRecordInvalidException()
        !unit.isValidRoutineUnit() -> RoutineUnitInvalidException()
        else -> null
    }
