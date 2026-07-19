package io.github.taetae98coding.diary.core.mapper.web

import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity

public fun Web.toLocal(): WebLocalEntity =
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

public fun WebLocalEntity.toRemote(): WebRemoteEntity =
    WebRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun WebRemoteEntity.toLocal(): WebLocalEntity =
    WebLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
