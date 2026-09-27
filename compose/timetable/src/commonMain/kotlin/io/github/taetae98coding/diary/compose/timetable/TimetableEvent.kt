package io.github.taetae98coding.diary.compose.timetable

import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime

public sealed interface TimetableEvent {
    public data class SelectTime(
        val start: LocalDateTime,
        val endInclusive: LocalDateTime,
    ) : TimetableEvent

    public data class SelectDate(
        val dateRange: LocalDateRange,
    ) : TimetableEvent
}
