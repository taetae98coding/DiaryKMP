package io.github.taetae98coding.diary.feature.calendar.ui.home.birthday

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday

internal data class CalendarHomeBirthdayUiState(
    val birthdayList: List<CalendarContactBirthday> = emptyList(),
)
