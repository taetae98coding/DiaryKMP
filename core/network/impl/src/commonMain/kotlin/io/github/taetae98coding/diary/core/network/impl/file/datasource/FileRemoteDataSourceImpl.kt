package io.github.taetae98coding.diary.core.network.impl.file.datasource

import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.transport.FileUploadTransport
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import kotlinx.coroutines.flow.Flow
import kotlinx.io.RawSource
import org.koin.core.annotation.Factory

@Factory
internal class FileRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
    private val fileUploadTransport: FileUploadTransport,
) : FileRemoteDataSource {
    override suspend fun upload(
        name: String,
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
        onSent: (sentBytes: Long) -> Unit,
    ): FileRemoteEntity =
        fileUploadTransport.upload(
            name = name,
            mimeType = mimeType,
            contentLength = contentLength,
            openContent = openContent,
            onSent = onSent,
        )

    override suspend fun fetch(
        cursor: FileCursorRemoteEntity?,
        size: Int,
    ): List<FileRemoteEntity> =
        supabaseFunction(
            function = LIST_FILE_FUNCTION,
            body = FileListRequestRemoteEntity(cursor = cursor, size = size),
        ).body<FileListResponseRemoteEntity>().fileList

    override fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?> = fileUploadTransport.getContinuedUpload()

    override fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity> = fileUploadTransport.getContinuedUploadResult()

    override suspend fun cancelContinuedUpload() {
        fileUploadTransport.cancelContinuedUpload()
    }

    private companion object {
        const val LIST_FILE_FUNCTION: String = "v1-file-list"
    }
}
