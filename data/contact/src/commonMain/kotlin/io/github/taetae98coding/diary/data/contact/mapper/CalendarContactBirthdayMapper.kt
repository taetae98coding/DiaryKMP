package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.LunarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.contact.LunarContactBirthday

internal fun CalendarContactBirthdayLocalEntity.toDomain(): CalendarContactBirthday =
    CalendarContactBirthday(
        contactId = contactId,
        name = name,
        nameQualifier = nameQualifier.trim(),
        date = birthdayDate,
    )

internal fun LunarContactBirthdayLocalEntity.toDomain(): LunarContactBirthday =
    LunarContactBirthday(
        contactId = contactId,
        name = name,
        nameQualifier = nameQualifier.trim(),
        birthday = birthday,
    )
