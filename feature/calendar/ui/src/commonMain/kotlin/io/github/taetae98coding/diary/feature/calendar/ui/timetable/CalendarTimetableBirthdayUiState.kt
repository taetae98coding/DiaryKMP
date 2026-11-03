package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday

internal data class CalendarTimetableBirthdayUiState(
    val birthdayList: List<CalendarContactBirthday> = emptyList(),
)
