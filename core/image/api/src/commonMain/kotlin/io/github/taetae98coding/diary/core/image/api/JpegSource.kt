package io.github.taetae98coding.diary.core.image.api

import kotlinx.io.RawSource

public interface JpegSource : AutoCloseable {
    public val size: Long

    public fun openSource(): RawSource
}
