package io.github.taetae98coding.diary.core.network.impl.file.datasource

import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileNotFoundRemoteException
import io.github.taetae98coding.diary.core.network.impl.di.FileDownloadHttpClient
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileDownloadResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileIdRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileListResponseRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.entity.FileUpdateRequestRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.file.transport.FileUploadTransport
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionException
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val COPY_BUFFER_SIZE = 64 * 1024

@Factory
internal class FileRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
    private val fileUploadTransport: FileUploadTransport,
    @FileDownloadHttpClient
    private val httpClient: HttpClient,
) : FileRemoteDataSource {
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
        fileUploadTransport.upload(
            name = name,
            title = title,
            description = description,
            mimeType = mimeType,
            contentLength = contentLength,
            accountId = accountId,
            openContent = openContent,
            onSent = onSent,
        )

    override suspend fun readList(
        cursor: FileCursorRemoteEntity?,
        size: Int,
    ): List<FileRemoteEntity> =
        supabaseFunction(
            function = LIST_FILE_FUNCTION,
            body = FileListRequestRemoteEntity(cursor = cursor, size = size),
        ).body<FileListResponseRemoteEntity>().fileList

    override suspend fun read(id: Uuid): FileRemoteEntity =
        invokeFileFunction(
            function = GET_FILE_FUNCTION,
            body = FileIdRequestRemoteEntity(id = id),
        ).body()

    override suspend fun update(
        id: Uuid,
        name: String,
        title: String,
        description: String,
    ): FileRemoteEntity =
        invokeFileFunction(
            function = UPDATE_FILE_FUNCTION,
            body = FileUpdateRequestRemoteEntity(id = id, name = name, title = title, description = description),
        ).body()

    override suspend fun download(
        id: Uuid,
        sink: RawSink,
    ) {
        val url =
            invokeFileFunction(
                function = DOWNLOAD_FILE_FUNCTION,
                body = FileIdRequestRemoteEntity(id = id),
            ).body<FileDownloadResponseRemoteEntity>().url

        httpClient.prepareGet(urlString = url).execute { response ->
            check(response.status.isSuccess()) { "File content request failed with ${response.status}." }

            val written = response.copyTo(sink = sink)
            val expected = response.contentLength()

            check(expected == null || expected == written) { "File content ended at $written of $expected bytes." }
        }
    }

    override fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?> = fileUploadTransport.getContinuedUpload()

    override fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity> = fileUploadTransport.getContinuedUploadResult()

    override suspend fun cancelContinuedUpload(exceptAccountId: Uuid?) {
        fileUploadTransport.cancelContinuedUpload(exceptAccountId = exceptAccountId)
    }

    private suspend inline fun <reified T : Any> invokeFileFunction(
        function: String,
        body: T,
    ): HttpResponse =
        try {
            supabaseFunction(function = function, body = body)
        } catch (exception: SupabaseFunctionException) {
            if (exception.statusCode == HttpStatusCode.NotFound.value) {
                throw FileNotFoundRemoteException(message = exception.message, cause = exception)
            }

            throw exception
        }

    private suspend fun HttpResponse.copyTo(sink: RawSink): Long {
        val channel = bodyAsChannel()
        val buffer = ByteArray(COPY_BUFFER_SIZE)
        val chunk = Buffer()
        var written = 0L

        while (true) {
            val count = channel.readAvailable(buffer)
            if (count < 0) break

            chunk.write(buffer, 0, count)
            sink.write(chunk, chunk.size)
            written += count
        }
        sink.flush()

        return written
    }

    private companion object {
        const val LIST_FILE_FUNCTION: String = "v1-file-list"
        const val GET_FILE_FUNCTION: String = "v1-file-get"
        const val UPDATE_FILE_FUNCTION: String = "v1-file-update"
        const val DOWNLOAD_FILE_FUNCTION: String = "v1-file-download"
    }
}
