package io.github.taetae98coding.diary.core.mapper.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPhoneNumberRemoteEntity

public fun ContactPhoneNumber.toLocal(): ContactPhoneNumberLocalEntity = ContactPhoneNumberLocalEntity(number = number)

public fun ContactPhoneNumberLocalEntity.toDomain(): ContactPhoneNumber = ContactPhoneNumber(number = number)

public fun ContactPhoneNumberLocalEntity.toRemote(): ContactPhoneNumberRemoteEntity = ContactPhoneNumberRemoteEntity(number = number)

public fun ContactPhoneNumberRemoteEntity.toLocal(): ContactPhoneNumberLocalEntity = ContactPhoneNumberLocalEntity(number = number)
