package io.github.taetae98coding.diary.core.network.impl.file.datasource

import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.network.impl.content.RawSourceContent
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListResponseRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLParameter
import kotlinx.io.RawSource
import org.koin.core.annotation.Factory
import kotlin.time.Duration.Companion.minutes

@Factory
internal class FileRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : FileRemoteDataSource {
    override suspend fun upload(
        name: String,
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
    ): FileRemoteEntity =
        try {
            supabaseFunction(
                function = UPLOAD_FILE_FUNCTION,
                body =
                    RawSourceContent(
                        contentType = ContentType.parse(mimeType),
                        contentLength = contentLength,
                        openContent = openContent,
                    ),
                // 헤더에는 ASCII만 실을 수 있어 파일 이름을 퍼센트 인코딩한다.
                headers = Headers.build { append(FILE_NAME_HEADER, name.encodeURLParameter()) },
                requestTimeout = UPLOAD_REQUEST_TIMEOUT,
            ).body()
        } catch (exception: SupabaseFunctionException) {
            if (exception.statusCode == HttpStatusCode.PayloadTooLarge.value) {
                throw FileTooLargeRemoteException(message = exception.message, cause = exception)
            }

            throw exception
        }

    override suspend fun fetch(
        cursor: FileCursorRemoteEntity?,
        size: Int,
    ): List<FileRemoteEntity> =
        supabaseFunction(
            function = LIST_FILE_FUNCTION,
            body = FileListRequestRemoteEntity(cursor = cursor, size = size),
        ).body<FileListResponseRemoteEntity>().fileList

    private companion object {
        const val UPLOAD_FILE_FUNCTION: String = "v1-file-upload"
        const val LIST_FILE_FUNCTION: String = "v1-file-list"
        const val FILE_NAME_HEADER: String = "X-File-Name"

        // 기본 요청 시간 제한은 짧은 JSON 요청에 맞춰져 있어 50MB까지 올리는 요청에는 따로 길게 둔다.
        val UPLOAD_REQUEST_TIMEOUT = 5.minutes
    }
}
