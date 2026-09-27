package io.github.taetae98coding.diary.domain.file.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUploadResult
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.flow.Flow

public interface FileRepository {
    public fun page(): Flow<PagingData<DiaryFile>>

    public suspend fun refresh()

    public suspend fun findSource(uri: FileUri): FileUploadSource

    public suspend fun create(
        source: FileUploadSource,
        title: String,
        description: String,
        onSent: (sentBytes: Long) -> Unit,
    ): DiaryFile

    public suspend fun addUploadSource(uri: FileUri)

    public suspend fun removeUploadSource(uri: FileUri)

    public suspend fun deleteLeftoverUploadSources()

    public fun getContinuedUpload(): Flow<ContinuedFileUpload?>

    public fun getContinuedUploadResult(): Flow<ContinuedFileUploadResult>

    public suspend fun deleteContinuedUpload()
}
