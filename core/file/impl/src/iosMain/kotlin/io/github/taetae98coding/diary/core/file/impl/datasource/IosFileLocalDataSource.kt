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
import platform.Foundation.NSTemporaryDirectory
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

    // 문서 선택기는 고른 파일을 앱의 임시 디렉터리에 사본으로 주므로 따로 붙들 권한이 없다.
    override suspend fun retain(uri: FileUri): Unit = Unit

    // 사본은 앱의 임시 디렉터리에만 생긴다. 그 밖의 위치는 앱이 만든 파일이 아닐 수 있어 지우지 않는다.
    override suspend fun release(uri: FileUri) {
        val path = uri.toPath().toString()

        if (path.startsWith(NSTemporaryDirectory().removeSuffix("/"))) {
            withContext(dispatcher) { SystemFileSystem.delete(Path(path), mustExist = false) }
        }
    }

    // 문서 선택기는 사본을 임시 디렉터리의 `-Inbox`로 끝나는 디렉터리에 둔다. 앱이 시작되는 시점에 그곳에 있는 사본은
    // 앞선 실행이 올리던 것이고, 시스템이 이어 올리는 전송은 따로 사본을 두므로 지워도 된다.
    override suspend fun deleteLeftoverCopies() {
        withContext(dispatcher) {
            val temporaryDirectory = Path(NSTemporaryDirectory())

            SystemFileSystem
                .list(temporaryDirectory)
                .filter { path -> path.name.endsWith(PICKER_COPY_DIRECTORY_SUFFIX) }
                .forEach { path -> path.deleteRecursively() }
        }
    }

    private fun FileUri.toUrl(): NSURL = checkNotNull(NSURL.URLWithString(value)) { "File uri is not a url. uri=$this" }

    private fun FileUri.toPath(): Path = Path(checkNotNull(toUrl().path) { "File uri has no path. uri=$this" })

    private fun Path.deleteRecursively() {
        if (SystemFileSystem.metadataOrNull(this)?.isDirectory == true) {
            SystemFileSystem.list(this).forEach { child -> child.deleteRecursively() }
        }

        SystemFileSystem.delete(this, mustExist = false)
    }

    private companion object {
        const val PICKER_COPY_DIRECTORY_SUFFIX: String = "-Inbox"
    }
}
