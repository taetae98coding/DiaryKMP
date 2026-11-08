package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayDetailLocalEntity
import io.github.taetae98coding.diary.core.model.dday.DDayDetail

public fun DDayDetail.toLocal(): DDayDetailLocalEntity =
    DDayDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeats,
        endDate = endDate,
        overflowPolicy = overflowPolicy.toLocal(),
    )

public fun DDayDetailLocalEntity.toDomain(): DDayDetail =
    DDayDetail(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeats,
        endDate = endDate,
        overflowPolicy = overflowPolicy.toDomain(),
    )
