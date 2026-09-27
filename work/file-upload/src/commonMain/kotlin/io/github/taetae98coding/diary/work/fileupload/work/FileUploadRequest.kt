package io.github.taetae98coding.diary.work.fileupload.work

import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import kotlin.uuid.Uuid

internal data class FileUploadRequest(
    val content: FileUploadContent,
    val accountId: Uuid,
)
