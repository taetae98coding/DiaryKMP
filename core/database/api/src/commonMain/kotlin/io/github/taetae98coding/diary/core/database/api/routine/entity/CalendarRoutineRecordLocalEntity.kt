package io.github.taetae98coding.diary.core.database.api.routine.entity

import androidx.room3.ColumnInfo
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public data class CalendarRoutineRecordLocalEntity(
    @ColumnInfo(name = "routine_id")
    val routineId: Uuid,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "color")
    val color: Long,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "amount")
    val amount: Long,
    @ColumnInfo(name = "unit")
    val unit: String,
)
