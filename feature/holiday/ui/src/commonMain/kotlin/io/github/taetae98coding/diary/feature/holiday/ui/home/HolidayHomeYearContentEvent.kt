package io.github.taetae98coding.diary.feature.holiday.ui.home

import kotlinx.datetime.LocalDateRange

internal sealed interface HolidayHomeYearContentEvent {
    data object ClickRetry : HolidayHomeYearContentEvent

    data class SelectDate(
        val dateRange: LocalDateRange,
    ) : HolidayHomeYearContentEvent
}
