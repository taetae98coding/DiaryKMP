package io.github.taetae98coding.diary.core.database.api.routine.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded

public data class RoutineSummaryLocalEntity(
    @Embedded
    val routine: RoutineLocalEntity,
    @ColumnInfo(name = "cycle_amount")
    val cycleAmount: Long,
)
