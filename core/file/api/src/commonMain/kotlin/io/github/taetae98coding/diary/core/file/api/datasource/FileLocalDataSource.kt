package io.github.taetae98coding.diary.core.file.api.datasource

import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.io.RawSource

public interface FileLocalDataSource {
    public suspend fun name(uri: FileUri): String

    public suspend fun mimeType(uri: FileUri): String

    public suspend fun size(uri: FileUri): Long

    public suspend fun openSource(uri: FileUri): RawSource

    public suspend fun delete(uri: FileUri)

    public suspend fun retain(uri: FileUri)

    public suspend fun release(uri: FileUri)

    public suspend fun deleteLeftoverCopies()
}
