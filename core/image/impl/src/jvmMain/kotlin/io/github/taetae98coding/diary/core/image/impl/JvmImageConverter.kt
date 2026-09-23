package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

private const val JPEG_FORMAT_NAME = "jpg"
private const val PERCENT = 100f

// EXIF는 파일 앞쪽 세그먼트에 있어, 방향을 읽으려고 사진 전체를 메모리에 올리지 않는다.
private const val EXIF_SCAN_BYTES = 64 * 1024

internal class JvmImageConverter : ImageConverter {
    override suspend fun toJpeg(
        uri: FileUri,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        jpegQuality: Int,
    ): JpegSource =
        withContext(Dispatchers.IO) {
            val sourceFile = Paths.get(URI(uri.value)).toFile()
            val image = checkNotNull(ImageIO.read(sourceFile)) { "Image cannot be read. uri=$uri" }
            val file = Files.createTempFile("profile-image", ".jpg").toFile()

            runCatching {
                image
                    .applyExifOrientation(sourceFile.readExifOrientation())
                    .cropScaled(cropRegion = cropRegion, maxSideLength = maxSideLength)
                    .writeJpeg(file = file, jpegQuality = jpegQuality)
            }.onFailure { file.delete() }
                .getOrThrow()

            FileJpegSource(path = Path(file.path))
        }

    private fun File.readExifOrientation(): Int = inputStream().use { stream -> stream.readNBytes(EXIF_SCAN_BYTES) }.readExifOrientation()

    // JPEG는 투명도를 담지 못해, 남길 영역을 불투명한 이미지에 옮겨 그리면서 함께 줄인다.
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

    private fun BufferedImage.writeJpeg(
        file: File,
        jpegQuality: Int,
    ) {
        val writers = ImageIO.getImageWritersByFormatName(JPEG_FORMAT_NAME)

        check(writers.hasNext()) { "Jpeg writer is not available." }

        val writer = writers.next()
        val writeParam =
            writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = jpegQuality / PERCENT
            }

        ImageIO.createImageOutputStream(file).use { output ->
            writer.output = output
            writer.write(null, IIOImage(this, null, null), writeParam)
        }
        writer.dispose()
    }
}
