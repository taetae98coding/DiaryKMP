package io.github.taetae98coding.diary.feature.routine.ui.home.record

import kotlin.uuid.Uuid

internal data class RoutineHomeRecord(
    val routineId: Uuid,
    val title: String,
    val unit: String,
    val amount: Int,
)
