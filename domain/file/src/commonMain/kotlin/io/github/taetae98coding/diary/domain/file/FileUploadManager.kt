package io.github.taetae98coding.diary.domain.file

import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface FileUploadManager {
    public val state: Flow<FileUploadState>

    public val event: Flow<FileUploadEvent>

    public suspend fun requestUpload(
        uri: FileUri,
        accountId: Uuid,
    )

    public suspend fun cancelUpload()

    public fun setFileHomeViewing(isViewing: Boolean)
}
