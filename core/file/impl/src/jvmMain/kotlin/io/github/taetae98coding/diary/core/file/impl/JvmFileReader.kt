package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.api.FileReader
import io.github.taetae98coding.diary.core.file.api.FileSource
import io.github.taetae98coding.diary.core.file.api.FileUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

internal class JvmFileReader : FileReader {
    override suspend fun open(uri: FileUri): FileSource =
        withContext(Dispatchers.IO) {
            val path = Paths.get(URI(uri.value))
            val mimeType = checkNotNull(Files.probeContentType(path)) { "File mime type is unknown. uri=$uri" }

            JvmFileSource(
                mimeType = mimeType,
                size = Files.size(path),
                path = Path(path.toString()),
            )
        }
}

private class JvmFileSource(
    override val mimeType: String,
    override val size: Long,
    private val path: Path,
) : FileSource {
    override fun openSource(): RawSource = SystemFileSystem.source(path)
}
