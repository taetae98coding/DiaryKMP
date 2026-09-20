package io.github.taetae98coding.diary.core.file.api

import io.github.taetae98coding.diary.core.model.file.FileUri

public interface FileReader {
    public suspend fun open(uri: FileUri): FileSource
}
