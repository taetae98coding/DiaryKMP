package io.github.taetae98coding.diary.work.fileupload.report

import io.github.taetae98coding.diary.core.model.file.ContinuedFileUploadResult
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import kotlin.uuid.Uuid

internal sealed interface FileUploadResult {
    data class Succeeded(
        val name: String,
        val fileId: Uuid,
    ) : FileUploadResult

    data object TooLarge : FileUploadResult

    data class Failed(
        val name: String,
    ) : FileUploadResult
}

internal fun FileUploadResult.toEvent(): FileUploadEvent =
    when (this) {
        is FileUploadResult.Succeeded -> FileUploadEvent.Succeeded(fileId = fileId)
        is FileUploadResult.TooLarge -> FileUploadEvent.TooLarge
        is FileUploadResult.Failed -> FileUploadEvent.Failed
    }

internal fun ContinuedFileUploadResult.toFileUploadResult(): FileUploadResult =
    when (this) {
        is ContinuedFileUploadResult.Succeeded -> FileUploadResult.Succeeded(name = name, fileId = file.id)
        is ContinuedFileUploadResult.TooLarge -> FileUploadResult.TooLarge
        is ContinuedFileUploadResult.Failed -> FileUploadResult.Failed(name = name)
    }
