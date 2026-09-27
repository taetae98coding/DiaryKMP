package io.github.taetae98coding.diary.domain.file

import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface FileUploadManager {
    public val state: Flow<FileUploadState>

    public fun getEvent(screen: FileScreen): Flow<FileUploadEvent>

    public suspend fun requestUpload(
        content: FileUploadContent,
        accountId: Uuid,
    )

    public suspend fun cancelUpload()

    public fun startViewing(screen: FileScreen)

    public fun stopViewing(screen: FileScreen)
}
