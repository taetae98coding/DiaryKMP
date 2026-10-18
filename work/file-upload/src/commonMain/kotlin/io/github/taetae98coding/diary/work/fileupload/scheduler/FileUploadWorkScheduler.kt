package io.github.taetae98coding.diary.work.fileupload.scheduler

import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

internal interface FileUploadWorkScheduler {
    val state: Flow<FileUploadState>

    suspend fun isUploading(): Boolean

    suspend fun upload(request: FileUploadRequest)

    suspend fun cancel(exceptAccountId: Uuid?): List<FileUri>
}
