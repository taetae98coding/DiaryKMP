package io.github.taetae98coding.diary.core.network.api.file.datasource

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.io.RawSource
import kotlin.uuid.Uuid

public interface FileRemoteDataSource {
    public suspend fun upload(
        name: String,
        title: String,
        description: String,
        mimeType: String,
        contentLength: Long,
        accountId: Uuid,
        openContent: suspend () -> RawSource,
        onSent: (sentBytes: Long) -> Unit,
    ): FileRemoteEntity

    public suspend fun fetch(
        cursor: FileCursorRemoteEntity?,
        size: Int,
    ): List<FileRemoteEntity>

    public fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?>

    public fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity>

    public suspend fun cancelContinuedUpload(exceptAccountId: Uuid?)
}
