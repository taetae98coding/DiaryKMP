package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.api.FileReader
import io.github.taetae98coding.diary.core.file.api.FileSource
import io.github.taetae98coding.diary.core.file.api.FileUri
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSURL
import platform.UniformTypeIdentifiers.UTType

internal class IosFileReader : FileReader {
    override suspend fun open(uri: FileUri): FileSource {
        val url = checkNotNull(NSURL.URLWithString(uri.value)) { "File uri is not a url. uri=$uri" }
        val path = Path(checkNotNull(url.path) { "File uri has no path. uri=$uri" })
        val mimeType =
            checkNotNull(UTType.typeWithFilenameExtension(url.pathExtension.orEmpty())?.preferredMIMEType) {
                "File mime type is unknown. uri=$uri"
            }
        val size = checkNotNull(SystemFileSystem.metadataOrNull(path)?.size) { "File size is unknown. uri=$uri" }

        return IosFileSource(mimeType = mimeType, size = size, path = path)
    }
}

private class IosFileSource(
    override val mimeType: String,
    override val size: Long,
    private val path: Path,
) : FileSource {
    override fun openSource(): RawSource = SystemFileSystem.source(path)
}
