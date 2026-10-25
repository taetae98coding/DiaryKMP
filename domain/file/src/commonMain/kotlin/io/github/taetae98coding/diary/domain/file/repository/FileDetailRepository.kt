package io.github.taetae98coding.diary.domain.file.repository

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface FileDetailRepository {
    public fun getUpdated(): Flow<Map<Uuid, DiaryFile>>

    public suspend fun fetch(id: Uuid): DiaryFile

    public suspend fun updateDetail(
        id: Uuid,
        detail: DiaryFileDetail,
    ): DiaryFile

    public suspend fun download(
        id: Uuid,
        directory: FileDownloadDirectory?,
        nameCandidate: (index: Int) -> String,
    ): FileDownloadResult
}
