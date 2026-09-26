package io.github.taetae98coding.diary.compose.calendar

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

public sealed interface CalendarEvent {
    public data class Select(
        val dateRange: LocalDateRange,
    ) : CalendarEvent

    public data class ClickDate(
        val date: LocalDate,
    ) : CalendarEvent

    public data class ClickWeek(
        val startDate: LocalDate,
    ) : CalendarEvent
}
