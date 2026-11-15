package io.github.taetae98coding.diary.core.database.api.routine.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDate

public data class RoutineDetailLocalEntity(
    @ColumnInfo(name = "title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String,
    @ColumnInfo(name = "color", defaultValue = "0")
    val color: Long,
    @ColumnInfo(name = "start_date", defaultValue = "'1970-01-01'")
    val startDate: LocalDate,
    @ColumnInfo(name = "cycle", defaultValue = "'FREQ=DAILY'")
    val cycle: RoutineCycleLocalEntity,
    @ColumnInfo(name = "unit", defaultValue = "")
    val unit: String,
    @ColumnInfo(name = "goal_amount", defaultValue = "NULL")
    val goalAmount: Int?,
    @ColumnInfo(name = "amount_per_record", defaultValue = "1")
    val amountPerRecord: Int,
)
