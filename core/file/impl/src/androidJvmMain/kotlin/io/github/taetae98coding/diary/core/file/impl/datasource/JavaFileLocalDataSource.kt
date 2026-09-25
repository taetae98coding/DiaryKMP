package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.file.impl.file.toFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.net.URLConnection

// file 위치만 다룬다. 플랫폼마다 다른 위치(Android의 content)는 이 구현을 감싸는 쪽이 덧붙인다.
internal class JavaFileLocalDataSource(
    private val dispatcher: CoroutineDispatcher,
) : FileLocalDataSource {
    override suspend fun name(uri: FileUri): String = uri.toFile().name

    // 파일 내용을 열지 않고 확장자로만 형식을 정한다. Android와 데스크톱이 같은 표를 쓰도록 JDK의 표를 쓴다.
    override suspend fun mimeType(uri: FileUri): String = URLConnection.guessContentTypeFromName(uri.toFile().name).orEmpty()

    // File.length()는 파일이 없거나 읽을 수 없어도 0을 돌려주므로, 빈 파일과 구분하려고 먼저 확인한다.
    override suspend fun size(uri: FileUri): Long =
        withContext(dispatcher) {
            val file = uri.toFile()

            check(file.isFile) { "File size cannot be read. uri=$uri" }
            file.length()
        }

    override suspend fun openSource(uri: FileUri): RawSource = withContext(dispatcher) { SystemFileSystem.source(Path(uri.toFile().path)) }

    override suspend fun delete(uri: FileUri) {
        withContext(dispatcher) { uri.toFile().delete() }
    }
}
