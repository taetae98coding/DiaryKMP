package io.github.taetae98coding.diary.core.file.api

public interface FileReader {
    public suspend fun open(uri: FileUri): FileSource
}
