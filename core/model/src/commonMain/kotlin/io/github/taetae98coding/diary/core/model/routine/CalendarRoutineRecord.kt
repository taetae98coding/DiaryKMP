package io.github.taetae98coding.diary.core.model.routine

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public data class CalendarRoutineRecord(
    val routineId: Uuid,
    val title: String,
    val color: Long,
    val date: LocalDate,
    val amount: Long,
    val unit: String,
)
