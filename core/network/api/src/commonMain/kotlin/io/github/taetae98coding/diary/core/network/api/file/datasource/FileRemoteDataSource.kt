package io.github.taetae98coding.diary.core.network.api.file.datasource

import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import kotlinx.io.RawSource

public interface FileRemoteDataSource {
    public suspend fun upload(
        name: String,
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
    ): FileRemoteEntity

    public suspend fun fetch(
        cursor: FileCursorRemoteEntity?,
        size: Int,
    ): List<FileRemoteEntity>
}
