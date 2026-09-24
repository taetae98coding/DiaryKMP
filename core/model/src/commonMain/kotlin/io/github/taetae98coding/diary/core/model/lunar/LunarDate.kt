package io.github.taetae98coding.diary.core.model.lunar

import kotlinx.datetime.LocalDate

public data class LunarDate(
    val solar: LocalDate,
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean,
)
