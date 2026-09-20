package io.github.taetae98coding.diary.core.image.api

import io.github.taetae98coding.diary.core.model.file.FileUri

public interface ImageConverter {
    public suspend fun toJpeg(uri: FileUri): JpegSource
}
