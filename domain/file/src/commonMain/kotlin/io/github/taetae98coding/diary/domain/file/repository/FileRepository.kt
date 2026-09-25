package io.github.taetae98coding.diary.domain.file.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.flow.Flow

public interface FileRepository {
    public fun page(): Flow<PagingData<DiaryFile>>

    public suspend fun create(
        uri: FileUri,
        maxSize: Long,
    ): DiaryFile
}
