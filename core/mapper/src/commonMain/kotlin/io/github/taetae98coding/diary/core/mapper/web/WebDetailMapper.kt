package io.github.taetae98coding.diary.core.mapper.web

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity

public fun WebDetail.toLocal(): WebDetailLocalEntity =
    WebDetailLocalEntity(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toLocal() },
    )

public fun WebDetailLocalEntity.toDomain(): WebDetail =
    WebDetail(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toDomain() },
    )

public fun WebDetailLocalEntity.toRemote(): WebDetailRemoteEntity =
    WebDetailRemoteEntity(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toRemote() },
    )

public fun WebDetailRemoteEntity.toLocal(): WebDetailLocalEntity =
    WebDetailLocalEntity(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toLocal() },
    )
