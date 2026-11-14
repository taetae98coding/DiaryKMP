package io.github.taetae98coding.diary.core.model.routine

import kotlinx.datetime.LocalDate

public data class RoutineDetail(
    val title: String,
    val description: String,
    val color: Long,
    val startDate: LocalDate,
    val cycle: RoutineCycle,
    val unit: String,
    val goalAmount: Int?,
    val amountPerRecord: Int,
)
