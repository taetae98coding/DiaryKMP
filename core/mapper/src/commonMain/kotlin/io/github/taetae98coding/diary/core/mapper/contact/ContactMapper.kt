package io.github.taetae98coding.diary.core.mapper.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity

public fun Contact.toLocal(): ContactLocalEntity =
    ContactLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun ContactLocalEntity.toDomain(): Contact =
    Contact(
        id = id,
        detail = detail.toDomain(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun ContactLocalEntity.toRemote(): ContactRemoteEntity =
    ContactRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun ContactRemoteEntity.toLocal(): ContactLocalEntity =
    ContactLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
