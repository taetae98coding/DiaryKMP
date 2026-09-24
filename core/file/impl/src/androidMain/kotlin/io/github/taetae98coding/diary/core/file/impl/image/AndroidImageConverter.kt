package io.github.taetae98coding.diary.core.file.impl.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import io.github.taetae98coding.diary.core.file.api.ImageConverter
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.file.impl.file.toFileUri
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Factory
internal class AndroidImageConverter(
    private val context: Context,
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : ImageConverter {
    override suspend fun convert(
        source: FileUri,
        format: ImageFormat,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        quality: Int,
    ): FileUri =
        withContext(dispatcher) {
            val decoderSource = ImageDecoder.createSource(context.contentResolver, Uri.parse(source.value))
            val bitmap =
                ImageDecoder.decodeBitmap(decoderSource) { decoder, info, _ ->
                    // 하드웨어 비트맵은 화소를 읽을 수 없어 다시 압축해 쓰지 못한다.
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.cropScaled(
                        imageSize = PixelSize(width = info.size.width, height = info.size.height),
                        cropRegion = cropRegion,
                        maxSideLength = maxSideLength,
                    )
                }
            val file = File(context.cacheDir, "converted-image-${UUID.randomUUID()}.${format.extension()}")

            runCatching {
                FileOutputStream(file).use { stream ->
                    check(bitmap.compress(format.toCompressFormat(), quality, stream)) {
                        "Image cannot be converted. uri=$source, format=$format"
                    }
                }
            }.onFailure { file.delete() }
                .getOrThrow()

            file.toFileUri()
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

    private fun ImageFormat.toCompressFormat(): Bitmap.CompressFormat =
        when (this) {
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        }

    private fun ImageFormat.extension(): String =
        when (this) {
            ImageFormat.JPEG -> "jpg"
        }
}
