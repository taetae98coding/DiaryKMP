package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber

internal fun ContactPhoneNumber.toLocal(): ContactPhoneNumberLocalEntity = ContactPhoneNumberLocalEntity(number = number)

internal fun ContactPhoneNumberLocalEntity.toDomain(): ContactPhoneNumber = ContactPhoneNumber(number = number)
