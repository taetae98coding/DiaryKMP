package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity

internal fun WebDetailLocalEntity.toRemote(): WebDetailRemoteEntity =
    WebDetailRemoteEntity(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toRemote() },
    )

internal fun WebDetailRemoteEntity.toLocal(): WebDetailLocalEntity =
    WebDetailLocalEntity(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toLocal() },
    )
