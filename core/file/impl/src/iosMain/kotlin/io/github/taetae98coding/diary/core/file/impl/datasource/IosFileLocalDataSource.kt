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
import platform.UniformTypeIdentifiers.UTType

@Factory
internal class IosFileLocalDataSource(
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : FileLocalDataSource {
    override suspend fun name(uri: FileUri): String = checkNotNull(uri.toUrl().lastPathComponent) { "File uri has no name. uri=$uri" }

    override suspend fun mimeType(uri: FileUri): String =
        uri
            .toUrl()
            .pathExtension
            ?.takeIf { extension -> extension.isNotEmpty() }
            ?.let { extension -> UTType.typeWithFilenameExtension(extension)?.preferredMIMEType }
            .orEmpty()

    override suspend fun size(uri: FileUri): Long =
        withContext(dispatcher) {
            checkNotNull(SystemFileSystem.metadataOrNull(uri.toPath())?.size) { "File size cannot be read. uri=$uri" }
        }

    override suspend fun openSource(uri: FileUri): RawSource = withContext(dispatcher) { SystemFileSystem.source(uri.toPath()) }

    override suspend fun delete(uri: FileUri) {
        withContext(dispatcher) { SystemFileSystem.delete(uri.toPath(), mustExist = false) }
    }

    private fun FileUri.toUrl(): NSURL = checkNotNull(NSURL.URLWithString(value)) { "File uri is not a url. uri=$this" }

    private fun FileUri.toPath(): Path = Path(checkNotNull(toUrl().path) { "File uri has no path. uri=$this" })
}
