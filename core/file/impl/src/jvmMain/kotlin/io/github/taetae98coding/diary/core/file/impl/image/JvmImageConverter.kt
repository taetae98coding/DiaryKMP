package io.github.taetae98coding.diary.core.file.impl.image

import io.github.taetae98coding.diary.core.file.api.ImageConverter
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.file.impl.file.toFile
import io.github.taetae98coding.diary.core.file.impl.file.toFileUri
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

private const val PERCENT = 100f

// EXIF는 파일 앞쪽 세그먼트에 있어, 방향을 읽으려고 사진 전체를 메모리에 올리지 않는다.
private const val EXIF_SCAN_BYTES = 64 * 1024

@Factory
internal class JvmImageConverter(
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
            val sourceFile = source.toFile()
            val image = checkNotNull(ImageIO.read(sourceFile)) { "Image cannot be read. uri=$source" }
            val file = Files.createTempFile("converted-image", ".${format.extension()}").toFile()

            runCatching {
                image
                    .applyExifOrientation(sourceFile.readExifOrientation())
                    .cropScaled(cropRegion = cropRegion, maxSideLength = maxSideLength)
                    .write(file = file, format = format, quality = quality)
            }.onFailure { file.delete() }
                .getOrThrow()

            file.toFileUri()
        }

    private fun File.readExifOrientation(): Int = inputStream().use { stream -> stream.readNBytes(EXIF_SCAN_BYTES) }.readExifOrientation()

    // 투명도를 담지 못하는 형식이 있어, 남길 영역을 불투명한 이미지에 옮겨 그리면서 함께 줄인다.
    private fun BufferedImage.cropScaled(
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
    ): BufferedImage {
        val crop = cropRegion.toPixelRect(imageWidth = width, imageHeight = height)
        val targetSize = crop.scaled(crop.scaleToFit(maxSideLength))
        val target = BufferedImage(targetSize.width, targetSize.height, BufferedImage.TYPE_INT_RGB)

        target.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            drawImage(
                this@cropScaled,
                0,
                0,
                targetSize.width,
                targetSize.height,
                crop.left,
                crop.top,
                crop.right,
                crop.bottom,
                null,
            )
            dispose()
        }

        return target
    }

    private fun BufferedImage.write(
        file: File,
        format: ImageFormat,
        quality: Int,
    ) {
        val writers = ImageIO.getImageWritersByFormatName(format.formatName())

        check(writers.hasNext()) { "Image writer is not available. format=$format" }

        val writer = writers.next()
        val writeParam =
            writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = quality / PERCENT
            }

        ImageIO.createImageOutputStream(file).use { output ->
            writer.output = output
            writer.write(null, IIOImage(this, null, null), writeParam)
        }
        writer.dispose()
    }

    private fun ImageFormat.formatName(): String =
        when (this) {
            ImageFormat.JPEG -> "jpg"
        }

    private fun ImageFormat.extension(): String =
        when (this) {
            ImageFormat.JPEG -> "jpg"
        }
}
