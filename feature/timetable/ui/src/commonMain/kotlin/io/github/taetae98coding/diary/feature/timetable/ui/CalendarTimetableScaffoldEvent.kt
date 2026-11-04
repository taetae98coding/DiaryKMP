package io.github.taetae98coding.diary.feature.timetable.ui

import kotlin.uuid.Uuid

internal sealed interface CalendarTimetableScaffoldEvent {
    data object ClickNavigateUp : CalendarTimetableScaffoldEvent

    data class ClickMemo(
        val id: Uuid,
    ) : CalendarTimetableScaffoldEvent

    data class ClickContactBirthday(
        val contactId: Uuid,
    ) : CalendarTimetableScaffoldEvent
}
