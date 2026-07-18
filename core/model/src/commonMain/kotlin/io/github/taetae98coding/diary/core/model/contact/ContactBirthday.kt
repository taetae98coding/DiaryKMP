package io.github.taetae98coding.diary.core.model.contact

import kotlinx.datetime.LocalDate

public data class ContactBirthday(
    val date: LocalDate,
    val calendar: ContactBirthdayCalendar,
)
