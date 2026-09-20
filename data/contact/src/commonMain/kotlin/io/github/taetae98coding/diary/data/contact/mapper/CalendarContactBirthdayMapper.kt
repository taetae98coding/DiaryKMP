package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday

internal fun CalendarContactBirthdayLocalEntity.toDomain(): CalendarContactBirthday =
    CalendarContactBirthday(
        contactId = contactId,
        name = name,
        date = birthdayDate,
    )
