package io.github.taetae98coding.diary.core.file.api

import kotlinx.io.RawSource

public interface FileSource {
    public val mimeType: String

    public val size: Long

    public fun openSource(): RawSource
}
