package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Factory
import platform.Foundation.NSURL

@Factory
internal class IosFileLocalDataSource(
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : FileLocalDataSource {
    override suspend fun size(uri: FileUri): Long =
        withContext(dispatcher) {
            SystemFileSystem.metadataOrNull(uri.toPath())?.size ?: 0
        }

    override suspend fun openSource(uri: FileUri): RawSource = withContext(dispatcher) { SystemFileSystem.source(uri.toPath()) }

    override suspend fun delete(uri: FileUri) {
        withContext(dispatcher) { SystemFileSystem.delete(uri.toPath(), mustExist = false) }
    }

    private fun FileUri.toPath(): Path {
        val url = checkNotNull(NSURL.URLWithString(value)) { "File uri is not a url. uri=$this" }

        return Path(checkNotNull(url.path) { "File uri has no path. uri=$this" })
    }
}
