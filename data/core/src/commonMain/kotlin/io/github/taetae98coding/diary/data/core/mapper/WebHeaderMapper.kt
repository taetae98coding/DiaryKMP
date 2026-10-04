package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebHeader

public fun WebHeader.toLocal(): WebHeaderLocalEntity =
    WebHeaderLocalEntity(
        name = name,
        value = value,
    )

public fun WebHeaderLocalEntity.toDomain(): WebHeader =
    WebHeader(
        name = name,
        value = value,
    )
