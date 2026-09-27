package io.github.taetae98coding.diary.work.fileupload.work

import io.github.taetae98coding.diary.core.model.file.FileUploadStep

internal interface FileUploadWork {
    suspend fun doWork(
        request: FileUploadRequest,
        onStep: suspend (FileUploadStep) -> Unit,
    )
}
