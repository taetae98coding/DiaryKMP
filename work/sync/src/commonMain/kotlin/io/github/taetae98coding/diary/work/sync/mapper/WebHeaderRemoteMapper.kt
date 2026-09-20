package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity

internal fun WebHeaderLocalEntity.toRemote(): WebHeaderRemoteEntity =
    WebHeaderRemoteEntity(
        name = name,
        value = value,
    )

internal fun WebHeaderRemoteEntity.toLocal(): WebHeaderLocalEntity =
    WebHeaderLocalEntity(
        name = name,
        value = value,
    )
