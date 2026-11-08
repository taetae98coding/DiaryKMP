package io.github.taetae98coding.diary.core.model.dday

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate

public data class DDayDetail(
    val title: String,
    val description: String,
    val color: Long,
    val baseDate: LocalDate,
    val countsBaseDay: Boolean,
    val repeats: Set<DateTimeUnit.DateBased>,
    val endDate: LocalDate?,
    val overflowPolicy: DDayOverflowPolicy,
)
