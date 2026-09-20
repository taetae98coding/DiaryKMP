package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity

internal fun ContactBirthdayCalendarLocalEntity.toRemote(): ContactBirthdayCalendarRemoteEntity =
    when (this) {
        ContactBirthdayCalendarLocalEntity.SOLAR -> ContactBirthdayCalendarRemoteEntity.SOLAR
        ContactBirthdayCalendarLocalEntity.LUNAR -> ContactBirthdayCalendarRemoteEntity.LUNAR
    }

internal fun ContactBirthdayCalendarRemoteEntity.toLocal(): ContactBirthdayCalendarLocalEntity =
    when (this) {
        ContactBirthdayCalendarRemoteEntity.SOLAR -> ContactBirthdayCalendarLocalEntity.SOLAR
        ContactBirthdayCalendarRemoteEntity.LUNAR -> ContactBirthdayCalendarLocalEntity.LUNAR
    }
