package io.github.taetae98coding.diary.core.model.holiday

import kotlinx.datetime.LocalDateRange

public data class Holiday(
    val name: String,
    val isHoliday: Boolean,
    val dateRange: LocalDateRange,
)
