package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageHeaderRemoteEntity

internal fun WebHeader.toLocal(): WebHeaderLocalEntity =
    WebHeaderLocalEntity(
        name = name,
        value = value,
    )

internal fun WebHeader.toRemote(): WebPageHeaderRemoteEntity =
    WebPageHeaderRemoteEntity(
        name = name,
        value = value,
    )

internal fun WebHeaderLocalEntity.toDomain(): WebHeader =
    WebHeader(
        name = name,
        value = value,
    )
