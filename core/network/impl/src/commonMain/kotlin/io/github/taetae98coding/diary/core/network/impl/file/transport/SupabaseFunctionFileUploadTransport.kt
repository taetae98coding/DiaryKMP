package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.io.RawSource
import kotlin.time.Duration
import kotlin.uuid.Uuid

// 앱이 살아 있는 동안에만 보내므로, 앞선 실행에서 이어지는 올리기는 없다.
internal class SupabaseFunctionFileUploadTransport(
    private val supabaseFunction: SupabaseFunction,
) : FileUploadTransport {
    override suspend fun upload(
        name: String,
        title: String,
        description: String,
        mimeType: String,
        contentLength: Long,
        accountId: Uuid,
        openContent: suspend () -> RawSource,
        onSent: (sentBytes: Long) -> Unit,
    ): FileRemoteEntity =
        try {
            supabaseFunction(
                function = UPLOAD_FILE_FUNCTION,
                body =
                    FileUploadMultipartContent(
                        multipart =
                            FileUploadMultipart(
                                name = name,
                                title = title,
                                description = description,
                                mimeType = mimeType,
                                contentLength = contentLength,
                            ),
                        openContent = openContent,
                        onSent = onSent,
                    ),
                requestTimeout = Duration.INFINITE,
            ).body()
        } catch (exception: SupabaseFunctionException) {
            if (exception.statusCode == HttpStatusCode.PayloadTooLarge.value) {
                throw FileTooLargeRemoteException(message = exception.message, cause = exception)
            }

            throw exception
        }

    override fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?> = flowOf(null)

    override fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity> = emptyFlow()

    override suspend fun cancelContinuedUpload(exceptAccountId: Uuid?): Unit = Unit
}
