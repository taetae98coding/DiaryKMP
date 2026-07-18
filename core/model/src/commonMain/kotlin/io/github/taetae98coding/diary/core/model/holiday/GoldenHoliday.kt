package io.github.taetae98coding.diary.core.model.holiday

import kotlinx.datetime.LocalDateRange

public data class GoldenHoliday(
    val dateRange: LocalDateRange,
    val holidayList: List<Holiday>,
    val annualLeaveDateRangeList: List<LocalDateRange>,
) {
    public val annualLeaveCount: Int
        get() = annualLeaveDateRangeList.sumOf { it.size }
}
