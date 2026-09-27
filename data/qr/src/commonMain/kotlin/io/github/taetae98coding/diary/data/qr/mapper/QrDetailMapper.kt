package io.github.taetae98coding.diary.data.qr.mapper

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.model.qr.QrDetail

internal fun QrDetail.toLocal(): QrDetailLocalEntity =
    QrDetailLocalEntity(
        title = title,
        description = description,
        value = value,
    )

internal fun QrDetailLocalEntity.toDomain(): QrDetail =
    QrDetail(
        title = title,
        description = description,
        value = value,
    )
