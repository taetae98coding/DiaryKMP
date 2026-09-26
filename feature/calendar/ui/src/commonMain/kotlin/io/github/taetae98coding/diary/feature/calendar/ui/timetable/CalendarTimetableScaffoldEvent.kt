package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import kotlin.uuid.Uuid

internal sealed interface CalendarTimetableScaffoldEvent {
    data object ClickNavigateUp : CalendarTimetableScaffoldEvent

    data class ClickMemo(
        val id: Uuid,
    ) : CalendarTimetableScaffoldEvent
}
