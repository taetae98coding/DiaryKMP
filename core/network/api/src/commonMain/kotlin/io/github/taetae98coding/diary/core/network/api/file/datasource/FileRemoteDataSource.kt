package io.github.taetae98coding.diary.core.network.api.file.datasource

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.io.RawSource

public interface FileRemoteDataSource {
    public suspend fun upload(
        name: String,
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
        onSent: (sentBytes: Long) -> Unit,
    ): FileRemoteEntity

    public suspend fun fetch(
        cursor: FileCursorRemoteEntity?,
        size: Int,
    ): List<FileRemoteEntity>

    public fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?>

    public fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity>

    public suspend fun cancelContinuedUpload()
}
