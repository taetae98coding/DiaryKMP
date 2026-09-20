package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar

internal fun ContactBirthdayCalendar.toLocal(): ContactBirthdayCalendarLocalEntity =
    when (this) {
        ContactBirthdayCalendar.SOLAR -> ContactBirthdayCalendarLocalEntity.SOLAR
        ContactBirthdayCalendar.LUNAR -> ContactBirthdayCalendarLocalEntity.LUNAR
    }

internal fun ContactBirthdayCalendarLocalEntity.toDomain(): ContactBirthdayCalendar =
    when (this) {
        ContactBirthdayCalendarLocalEntity.SOLAR -> ContactBirthdayCalendar.SOLAR
        ContactBirthdayCalendarLocalEntity.LUNAR -> ContactBirthdayCalendar.LUNAR
    }
