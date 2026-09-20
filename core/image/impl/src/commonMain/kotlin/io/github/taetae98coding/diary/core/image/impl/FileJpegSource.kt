package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.image.api.JpegSource
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

internal class FileJpegSource(
    private val path: Path,
) : JpegSource {
    override val size: Long = SystemFileSystem.metadataOrNull(path)?.size ?: 0

    override fun openSource(): RawSource = SystemFileSystem.source(path)

    override fun close() {
        SystemFileSystem.delete(path, mustExist = false)
    }
}
