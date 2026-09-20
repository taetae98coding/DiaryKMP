package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPhoneNumberRemoteEntity

internal fun ContactPhoneNumberLocalEntity.toRemote(): ContactPhoneNumberRemoteEntity = ContactPhoneNumberRemoteEntity(number = number)

internal fun ContactPhoneNumberRemoteEntity.toLocal(): ContactPhoneNumberLocalEntity = ContactPhoneNumberLocalEntity(number = number)
