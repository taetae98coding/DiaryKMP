package io.github.taetae98coding.diary.core.file.api

import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat

public interface ImageConverter {
    // 돌려준 위치의 파일은 호출자가 다 쓰고 지운다.
    public suspend fun convert(
        source: FileUri,
        format: ImageFormat,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        quality: Int,
    ): FileUri
}
