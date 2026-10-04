package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar

public fun ContactBirthdayCalendar.toLocal(): ContactBirthdayCalendarLocalEntity =
    when (this) {
        ContactBirthdayCalendar.SOLAR -> ContactBirthdayCalendarLocalEntity.SOLAR
        ContactBirthdayCalendar.LUNAR -> ContactBirthdayCalendarLocalEntity.LUNAR
    }

public fun ContactBirthdayCalendarLocalEntity.toDomain(): ContactBirthdayCalendar =
    when (this) {
        ContactBirthdayCalendarLocalEntity.SOLAR -> ContactBirthdayCalendar.SOLAR
        ContactBirthdayCalendarLocalEntity.LUNAR -> ContactBirthdayCalendar.LUNAR
    }
