package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.network.impl.content.RawSourceContent
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.io.RawSource
import kotlin.time.Duration

// 앱이 살아 있는 동안에만 보내므로, 앞선 실행에서 이어지는 올리기는 없다.
internal class SupabaseFunctionFileUploadTransport(
    private val supabaseFunction: SupabaseFunction,
) : FileUploadTransport {
    override suspend fun upload(
        name: String,
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
        onSent: (sentBytes: Long) -> Unit,
    ): FileRemoteEntity =
        try {
            supabaseFunction(
                function = UPLOAD_FILE_FUNCTION,
                body =
                    RawSourceContent(
                        contentType = ContentType.parse(mimeType),
                        contentLength = contentLength,
                        openContent = openContent,
                        onSent = onSent,
                    ),
                // 헤더에는 ASCII만 실을 수 있어 파일 이름을 퍼센트 인코딩한다.
                headers = Headers.build { append(FILE_NAME_HEADER, name.encodeURLParameter()) },
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

    override suspend fun cancelContinuedUpload(): Unit = Unit
}
