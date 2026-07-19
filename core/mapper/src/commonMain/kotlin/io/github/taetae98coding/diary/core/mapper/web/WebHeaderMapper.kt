package io.github.taetae98coding.diary.core.mapper.web

import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageHeaderRemoteEntity

public fun WebHeader.toLocal(): WebHeaderLocalEntity =
    WebHeaderLocalEntity(
        name = name,
        value = value,
    )

public fun WebHeader.toRemote(): WebPageHeaderRemoteEntity =
    WebPageHeaderRemoteEntity(
        name = name,
        value = value,
    )

public fun WebHeaderLocalEntity.toDomain(): WebHeader =
    WebHeader(
        name = name,
        value = value,
    )

public fun WebHeaderLocalEntity.toRemote(): WebHeaderRemoteEntity =
    WebHeaderRemoteEntity(
        name = name,
        value = value,
    )

public fun WebHeaderRemoteEntity.toLocal(): WebHeaderLocalEntity =
    WebHeaderLocalEntity(
        name = name,
        value = value,
    )
