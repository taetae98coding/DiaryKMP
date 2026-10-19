package io.github.taetae98coding.diary.core.network.api.file.datasource

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.io.RawSink
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

    public suspend fun readList(
        cursor: FileCursorRemoteEntity?,
        size: Int,
    ): List<FileRemoteEntity>

    public suspend fun read(id: Uuid): FileRemoteEntity

    public suspend fun update(
        id: Uuid,
        name: String,
        title: String,
        description: String,
    ): FileRemoteEntity

    // 서버가 내주는 임시 주소에서 내용을 받아 sink에 끝까지 쓴다. 다 쓰기 전에 실패하면 예외를 던진다.
    public suspend fun download(
        id: Uuid,
        sink: RawSink,
    )

    public fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?>

    public fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity>

    public suspend fun cancelContinuedUpload(exceptAccountId: Uuid?)
}
