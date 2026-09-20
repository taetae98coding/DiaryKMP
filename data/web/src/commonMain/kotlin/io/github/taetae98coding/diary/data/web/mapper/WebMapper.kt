package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.model.web.Web

internal fun Web.toLocal(): WebLocalEntity =
    WebLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun WebLocalEntity.toDomain(): Web =
    Web(
        id = id,
        detail = detail.toDomain(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
