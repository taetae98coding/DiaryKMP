package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayDetailRemoteEntity

internal fun DDayDetailLocalEntity.toRemote(): DDayDetailRemoteEntity =
    DDayDetailRemoteEntity(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeats.map { repeat -> repeat.toRemote() },
        endDate = endDate,
        overflowPolicy = overflowPolicy.toRemote(),
    )

internal fun DDayDetailRemoteEntity.toLocal(): DDayDetailLocalEntity =
    DDayDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeats.mapTo(mutableSetOf()) { repeat -> repeat.toLocal() },
        endDate = endDate,
        overflowPolicy = overflowPolicy.toLocal(),
    )
