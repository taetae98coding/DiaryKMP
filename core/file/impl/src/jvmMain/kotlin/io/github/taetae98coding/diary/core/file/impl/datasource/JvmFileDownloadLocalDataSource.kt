package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.file.api.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.file.impl.di.FileDownloadDefaultDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.asSink
import org.koin.core.annotation.Factory
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

@Factory
internal class JvmFileDownloadLocalDataSource(
    @FileDispatcher
    private val dispatcher: CoroutineDispatcher,
    @FileDownloadDefaultDirectory
    private val defaultDirectory: String,
    private val directoryBookmark: DirectoryBookmark,
) : FileDownloadLocalDataSource {
    override val isDirectorySelectable: Boolean = true

    override suspend fun save(
        directory: FileDownloadDirectory?,
        nameCandidate: (index: Int) -> String,
        write: suspend (RawSink) -> Unit,
    ): FileDownloadResult =
        withContext(dispatcher) {
            val name =
                if (directory == null) {
                    defaultPath().saveNewFile(nameCandidate = nameCandidate, write = write)
                } else {
                    directory.accessScoped { directory.toPath().saveNewFile(nameCandidate = nameCandidate, write = write) }
                }

            FileDownloadResult.Saved(name = name)
        }

    override suspend fun retainDirectory(uri: String): FileDownloadDirectory =
        withContext(dispatcher) {
            val path = Paths.get(uri).toAbsolutePath()

            check(Files.isDirectory(path)) { "Not a directory. path=$path" }

            FileDownloadDirectory(
                uri = path.toString(),
                name = path.fileName?.toString() ?: path.toString(),
                bookmark = directoryBookmark.create(path = path.toString()),
            )
        }

    override suspend fun releaseDirectory(directory: FileDownloadDirectory): Unit = Unit

    // 북마크 없이 저장한 폴더는 경로만으로 연다.
    private suspend fun <T> FileDownloadDirectory.accessScoped(block: suspend () -> T): T {
        if (bookmark.isEmpty()) return block()

        val access =
            try {
                directoryBookmark.startAccessing(bookmark = bookmark)
            } catch (exception: IllegalStateException) {
                throw FileDownloadDirectoryUnavailableException(message = "Bookmark cannot be resolved. uri=$uri", cause = exception)
            }

        return access.use { block() }
    }

    private suspend fun Path.saveNewFile(
        nameCandidate: (index: Int) -> String,
        write: suspend (RawSink) -> Unit,
    ): String {
        val (path, name) = createNewFile(nameCandidate = nameCandidate)

        try {
            Files.newOutputStream(path).asSink().use { sink -> write(sink) }
        } catch (throwable: Throwable) {
            Files.deleteIfExists(path)
            throw throwable
        }

        return name
    }

    private fun defaultPath(): Path =
        try {
            Files.createDirectories(Paths.get(defaultDirectory))
        } catch (exception: IOException) {
            throw FileDownloadDirectoryUnavailableException(message = "Default directory cannot be created. path=$defaultDirectory", cause = exception)
        }

    private fun FileDownloadDirectory.toPath(): Path {
        val path =
            try {
                Paths.get(uri)
            } catch (exception: InvalidPathException) {
                throw FileDownloadDirectoryUnavailableException(message = "Invalid directory. uri=$uri", cause = exception)
            }

        if (!Files.isDirectory(path) || !Files.isWritable(path)) {
            throw FileDownloadDirectoryUnavailableException(message = "Directory is not writable. path=$path")
        }

        return path
    }

    // 고른 이름이 그사이 생겼을 수 있어 만들기 자체로 겹치는지 판단한다.
    private suspend fun Path.createNewFile(nameCandidate: (index: Int) -> String): Pair<Path, String> {
        var index = 0

        while (true) {
            currentCoroutineContext().ensureActive()
            val name = nameCandidate(index)
            val path = resolve(name)

            try {
                return Files.createFile(path) to name
            } catch (_: FileAlreadyExistsException) {
                index++
            } catch (exception: IOException) {
                throw FileDownloadDirectoryUnavailableException(message = "Couldn't create a file. path=$path", cause = exception)
            }
        }
    }
}
