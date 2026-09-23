package io.github.taetae98coding.diary.core.image.api

import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion

public interface ImageConverter {
    public suspend fun toJpeg(
        uri: FileUri,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        jpegQuality: Int,
    ): JpegSource
}
