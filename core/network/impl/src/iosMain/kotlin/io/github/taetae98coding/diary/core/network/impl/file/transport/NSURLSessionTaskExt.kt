package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.ktor.http.HttpStatusCode
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURLSessionTask

internal fun NSURLSessionTask.toResult(
    error: NSError?,
    responseBody: String,
): Result<FileRemoteEntity> {
    val statusCode = (response as? NSHTTPURLResponse)?.statusCode?.toInt()

    return when {
        error != null -> Result.failure(IllegalStateException("File upload failed. error=${error.localizedDescription}"))

        statusCode == HttpStatusCode.PayloadTooLarge.value -> Result.failure(FileTooLargeRemoteException(message = responseBody))

        statusCode != null && statusCode in HttpStatusCode.OK.value..<HttpStatusCode.MultipleChoices.value ->
            runCatching { uploadTaskJson.decodeFromString<FileRemoteEntity>(responseBody) }

        else -> Result.failure(IllegalStateException("File upload failed. status=$statusCode, body=$responseBody"))
    }
}

internal fun Result<FileRemoteEntity>.toContinuedResult(name: String): ContinuedFileUploadResultRemoteEntity =
    fold(
        onSuccess = { file -> ContinuedFileUploadResultRemoteEntity.Succeeded(name = name, file = file) },
        onFailure = { throwable ->
            if (throwable is FileTooLargeRemoteException) {
                ContinuedFileUploadResultRemoteEntity.TooLarge(name = name)
            } else {
                ContinuedFileUploadResultRemoteEntity.Failed(name = name)
            }
        },
    )

internal fun NSURLSessionTask.toContinuedUpload(
    totalSentBytes: Long,
    totalLength: Long,
): ContinuedFileUploadRemoteEntity {
    val description = uploadTaskDescription
    val contentLength = description?.contentLength ?: totalLength
    val headBytes = description?.headBytes ?: 0

    return ContinuedFileUploadRemoteEntity(
        name = description?.name.orEmpty(),
        contentLength = contentLength,
        sentBytes = (totalSentBytes - headBytes).coerceIn(0, contentLength),
    )
}
