package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber

public fun ContactPhoneNumber.toLocal(): ContactPhoneNumberLocalEntity = ContactPhoneNumberLocalEntity(number = number)

public fun ContactPhoneNumberLocalEntity.toDomain(): ContactPhoneNumber = ContactPhoneNumber(number = number)
