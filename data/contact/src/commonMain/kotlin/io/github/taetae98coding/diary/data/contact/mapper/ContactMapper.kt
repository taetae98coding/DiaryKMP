package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.model.contact.Contact

internal fun Contact.toLocal(): ContactLocalEntity =
    ContactLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isFavorite = isFavorite,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun ContactLocalEntity.toDomain(): Contact =
    Contact(
        id = id,
        detail = detail.toDomain(),
        isFavorite = isFavorite,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
