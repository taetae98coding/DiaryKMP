@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.file.api.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.file.impl.di.FileDownloadDefaultDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.io.RawSink
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Factory
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create

@Factory
internal class IosFileDownloadLocalDataSource(
    @FileDispatcher
    private val dispatcher: CoroutineDispatcher,
    @FileDownloadDefaultDirectory
    private val defaultDirectory: String,
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
                    Path(defaultDirectory).saveNewFile(nameCandidate = nameCandidate, write = write)
                } else {
                    directory.accessScoped { path -> path.saveNewFile(nameCandidate = nameCandidate, write = write) }
                }

            FileDownloadResult.Saved(name = name)
        }

    // 폴더 선택 도구가 준 위치는 이번 실행에서만 열 수 있어, 다음 실행에도 열 수 있는 북마크로 바꿔 둔다.
    override suspend fun retainDirectory(uri: String): FileDownloadDirectory =
        withContext(dispatcher) {
            val url = checkNotNull(NSURL.URLWithString(uri)) { "Invalid directory uri. uri=$uri" }
            val isAccessing = url.startAccessingSecurityScopedResource()

            try {
                val bookmark =
                    memScoped {
                        val error = alloc<ObjCObjectVar<NSError?>>()
                        url.bookmarkDataWithOptions(
                            options = 0uL,
                            includingResourceValuesForKeys = null,
                            relativeToURL = null,
                            error = error.ptr,
                        ) ?: error("Bookmark cannot be created. error=${error.value?.localizedDescription}")
                    }

                FileDownloadDirectory(
                    uri = bookmark.base64EncodedStringWithOptions(0uL),
                    name = url.lastPathComponent.orEmpty(),
                )
            } finally {
                if (isAccessing) url.stopAccessingSecurityScopedResource()
            }
        }

    // 북마크는 앱이 가진 기록일 뿐이라 시스템에 돌려줄 허락이 없다.
    override suspend fun releaseDirectory(directory: FileDownloadDirectory): Unit = Unit

    private suspend fun <T> FileDownloadDirectory.accessScoped(block: suspend (Path) -> T): T {
        val url = resolveBookmark()
        val path = url.path ?: throw FileDownloadDirectoryUnavailableException(message = "Bookmark has no path.")

        if (!url.startAccessingSecurityScopedResource()) {
            throw FileDownloadDirectoryUnavailableException(message = "Directory cannot be accessed. path=$path")
        }

        return try {
            block(Path(path))
        } finally {
            url.stopAccessingSecurityScopedResource()
        }
    }

    private fun FileDownloadDirectory.resolveBookmark(): NSURL {
        val data = NSData.create(base64EncodedString = uri, options = 0uL) ?: throw FileDownloadDirectoryUnavailableException(message = "Bookmark is invalid.")

        return memScoped {
            val isStale = alloc<BooleanVar>()
            val error = alloc<ObjCObjectVar<NSError?>>()

            NSURL.URLByResolvingBookmarkData(
                bookmarkData = data,
                options = 0uL,
                relativeToURL = null,
                bookmarkDataIsStale = isStale.ptr,
                error = error.ptr,
            ) ?: throw FileDownloadDirectoryUnavailableException(message = "Bookmark cannot be resolved. error=${error.value?.localizedDescription}")
        }
    }

    private suspend fun Path.saveNewFile(
        nameCandidate: (index: Int) -> String,
        write: suspend (RawSink) -> Unit,
    ): String {
        val name = availableName(nameCandidate = nameCandidate)
        val path = Path(this, name)
        val fileSink = openNewFile(path = path)

        try {
            fileSink.use { sink -> write(sink) }
        } catch (throwable: Throwable) {
            SystemFileSystem.delete(path, mustExist = false)
            throw throwable
        }

        return name
    }

    private suspend fun Path.availableName(nameCandidate: (index: Int) -> String): String {
        var index = 0

        while (true) {
            currentCoroutineContext().ensureActive()

            val name = nameCandidate(index)
            if (!SystemFileSystem.exists(Path(this, name))) return name

            index++
        }
    }

    private fun Path.openNewFile(path: Path): RawSink {
        if (SystemFileSystem.metadataOrNull(this)?.isDirectory != true) {
            throw FileDownloadDirectoryUnavailableException(message = "Directory is gone. path=$this")
        }

        return try {
            SystemFileSystem.sink(path)
        } catch (exception: IOException) {
            throw FileDownloadDirectoryUnavailableException(message = "File cannot be created. path=$path", cause = exception)
        }
    }
}
