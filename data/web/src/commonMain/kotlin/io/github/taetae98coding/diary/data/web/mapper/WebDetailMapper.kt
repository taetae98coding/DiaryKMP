package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebDetail

internal fun WebDetail.toLocal(): WebDetailLocalEntity =
    WebDetailLocalEntity(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toLocal() },
    )

internal fun WebDetailLocalEntity.toDomain(): WebDetail =
    WebDetail(
        title = title,
        description = description,
        url = url,
        headerList = headerList.map { header -> header.toDomain() },
    )
