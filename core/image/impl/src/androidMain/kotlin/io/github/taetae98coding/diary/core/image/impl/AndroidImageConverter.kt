package io.github.taetae98coding.diary.core.image.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal class AndroidImageConverter(
    private val context: Context,
) : ImageConverter {
    override suspend fun toJpeg(
        uri: FileUri,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
    ): JpegSource =
        withContext(Dispatchers.IO) {
            val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri.value))
            val bitmap =
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    // 하드웨어 비트맵은 화소를 읽을 수 없어 JPEG로 다시 쓰지 못한다.
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.cropScaled(
                        imageSize = PixelSize(width = info.size.width, height = info.size.height),
                        cropRegion = cropRegion,
                        maxSideLength = maxSideLength,
                    )
                }
            val file = File(context.cacheDir, "profile-image-${UUID.randomUUID()}.jpg")

            runCatching {
                FileOutputStream(file).use { stream ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY_PERCENT, stream)) {
                        "Image cannot be converted to jpeg. uri=$uri"
                    }
                }
            }.onFailure { file.delete() }
                .getOrThrow()

            FileJpegSource(path = Path(file.path))
        }

    // ImageDecoder는 촬영 방향을 반영한 크기를 알리고, 자르는 영역은 목표 크기로 줄인 뒤의 좌표로 받는다.
    private fun ImageDecoder.cropScaled(
        imageSize: PixelSize,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
    ) {
        val scale = cropRegion.toPixelRect(imageWidth = imageSize.width, imageHeight = imageSize.height).scaleToFit(maxSideLength)
        val targetSize = imageSize.scaled(scale)

        if (scale < 1.0) {
            setTargetSize(targetSize.width, targetSize.height)
        }

        val crop = cropRegion.toPixelRect(imageWidth = targetSize.width, imageHeight = targetSize.height)

        setCrop(Rect(crop.left, crop.top, crop.right, crop.bottom))
    }
}
