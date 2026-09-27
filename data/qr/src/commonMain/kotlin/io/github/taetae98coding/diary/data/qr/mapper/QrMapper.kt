package io.github.taetae98coding.diary.data.qr.mapper

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.model.qr.Qr

internal fun Qr.toLocal(): QrLocalEntity =
    QrLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun QrLocalEntity.toDomain(): Qr =
    Qr(
        id = id,
        detail = detail.toDomain(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
