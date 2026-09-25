package io.github.taetae98coding.diary.data.file.mapper

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity

internal fun FileRemoteEntity.toDomain(): DiaryFile =
    DiaryFile(
        id = id,
        name = name,
        mimeType = mimeType,
        size = size,
        createdAt = createdAt,
    )

internal fun FileRemoteEntity.toCursor(): FileCursorRemoteEntity =
    FileCursorRemoteEntity(
        createdAt = createdAt,
        id = id,
    )
