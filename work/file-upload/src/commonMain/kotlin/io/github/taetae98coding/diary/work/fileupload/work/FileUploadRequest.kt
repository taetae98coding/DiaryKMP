package io.github.taetae98coding.diary.work.fileupload.work

import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlin.uuid.Uuid

internal data class FileUploadRequest(
    val uri: FileUri,
    val accountId: Uuid,
)
