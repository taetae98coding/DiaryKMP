package io.github.taetae98coding.diary.core.model.dday

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public data class CalendarDDay(
    val ddayId: Uuid,
    val title: String,
    val color: Long,
    val date: LocalDate,
    val label: CalendarDDayLabel,
)
