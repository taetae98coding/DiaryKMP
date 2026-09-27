package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrDetailRemoteEntity

internal fun QrDetailLocalEntity.toRemote(): QrDetailRemoteEntity =
    QrDetailRemoteEntity(
        title = title,
        description = description,
        value = value,
    )

internal fun QrDetailRemoteEntity.toLocal(): QrDetailLocalEntity =
    QrDetailLocalEntity(
        title = title,
        description = description,
        value = value,
    )
