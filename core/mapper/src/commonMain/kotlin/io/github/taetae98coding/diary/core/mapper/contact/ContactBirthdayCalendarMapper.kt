package io.github.taetae98coding.diary.core.mapper.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity

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

public fun ContactBirthdayCalendarLocalEntity.toRemote(): ContactBirthdayCalendarRemoteEntity =
    when (this) {
        ContactBirthdayCalendarLocalEntity.SOLAR -> ContactBirthdayCalendarRemoteEntity.SOLAR
        ContactBirthdayCalendarLocalEntity.LUNAR -> ContactBirthdayCalendarRemoteEntity.LUNAR
    }

public fun ContactBirthdayCalendarRemoteEntity.toLocal(): ContactBirthdayCalendarLocalEntity =
    when (this) {
        ContactBirthdayCalendarRemoteEntity.SOLAR -> ContactBirthdayCalendarLocalEntity.SOLAR
        ContactBirthdayCalendarRemoteEntity.LUNAR -> ContactBirthdayCalendarLocalEntity.LUNAR
    }
