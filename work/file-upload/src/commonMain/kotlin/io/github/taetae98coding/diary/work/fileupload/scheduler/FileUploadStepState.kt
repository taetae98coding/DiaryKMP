package io.github.taetae98coding.diary.work.fileupload.scheduler

import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.model.file.uploadPercentOrNull

internal fun FileUploadStep.toFileUploadState(): FileUploadState.Uploading =
    when (this) {
        is FileUploadStep.Started -> FileUploadState.Uploading(percent = null)
        is FileUploadStep.Sent -> FileUploadState.Uploading(percent = uploadPercentOrNull(size = source.size, sentBytes = sentBytes))
        is FileUploadStep.Completed -> FileUploadState.Uploading(percent = uploadPercentOrNull(size = source.size, sentBytes = source.size))
    }

internal fun ContinuedFileUpload.toFileUploadState(): FileUploadState.Uploading = FileUploadState.Uploading(percent = uploadPercentOrNull(size = size, sentBytes = sentBytes))
