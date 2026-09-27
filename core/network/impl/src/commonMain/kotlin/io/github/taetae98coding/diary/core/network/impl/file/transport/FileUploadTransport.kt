package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.io.RawSource

internal const val UPLOAD_FILE_FUNCTION: String = "v1-file-upload"
internal const val FILE_NAME_HEADER: String = "X-File-Name"

// iOS는 앱이 멈추거나 정리되어도 시스템이 전송을 이어 가는 수단으로 보내고, 그 밖의 플랫폼은 앱의 HTTP 클라이언트로 보낸다.
internal interface FileUploadTransport {
    suspend fun upload(
        name: String,
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
        onSent: (sentBytes: Long) -> Unit,
    ): FileRemoteEntity

    fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?>

    fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity>

    suspend fun cancelContinuedUpload()
}
