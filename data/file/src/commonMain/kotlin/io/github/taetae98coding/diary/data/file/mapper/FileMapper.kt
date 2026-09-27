package io.github.taetae98coding.diary.data.file.mapper

import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUploadResult
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity

internal fun FileRemoteEntity.toDomain(): DiaryFile =
    DiaryFile(
        id = id,
        name = name,
        title = title,
        description = description,
        mimeType = mimeType,
        size = size,
        createdAt = createdAt,
    )

internal fun FileRemoteEntity.toCursor(): FileCursorRemoteEntity =
    FileCursorRemoteEntity(
        createdAt = createdAt,
        id = id,
    )

internal fun ContinuedFileUploadRemoteEntity.toDomain(): ContinuedFileUpload =
    ContinuedFileUpload(
        name = name,
        size = contentLength,
        sentBytes = sentBytes,
    )

internal fun ContinuedFileUploadResultRemoteEntity.toDomain(): ContinuedFileUploadResult =
    when (this) {
        is ContinuedFileUploadResultRemoteEntity.Succeeded -> ContinuedFileUploadResult.Succeeded(name = name, file = file.toDomain())
        is ContinuedFileUploadResultRemoteEntity.TooLarge -> ContinuedFileUploadResult.TooLarge(name = name)
        is ContinuedFileUploadResultRemoteEntity.Failed -> ContinuedFileUploadResult.Failed(name = name)
    }
