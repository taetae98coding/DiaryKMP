package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.file.impl.file.toFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

// file 위치만 다룬다. 플랫폼마다 다른 위치(Android의 content)는 이 구현을 감싸는 쪽이 덧붙인다.
internal class JavaFileLocalDataSource(
    private val dispatcher: CoroutineDispatcher,
) : FileLocalDataSource {
    override suspend fun size(uri: FileUri): Long = withContext(dispatcher) { uri.toFile().length() }

    override suspend fun openSource(uri: FileUri): RawSource = withContext(dispatcher) { SystemFileSystem.source(Path(uri.toFile().path)) }

    override suspend fun delete(uri: FileUri) {
        withContext(dispatcher) { uri.toFile().delete() }
    }
}
