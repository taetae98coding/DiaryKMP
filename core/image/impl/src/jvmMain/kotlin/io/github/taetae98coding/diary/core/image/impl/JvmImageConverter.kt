package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
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
    override suspend fun toJpeg(uri: FileUri): JpegSource =
        withContext(Dispatchers.IO) {
            val sourceFile = Paths.get(URI(uri.value)).toFile()
            val image = checkNotNull(ImageIO.read(sourceFile)) { "Image cannot be read. uri=$uri" }
            val file = Files.createTempFile("profile-image", ".jpg").toFile()

            runCatching {
                image
                    .applyExifOrientation(sourceFile.readExifOrientation())
                    .toOpaque()
                    .writeJpeg(file)
            }.onFailure { file.delete() }
                .getOrThrow()

            FileJpegSource(path = Path(file.path))
        }

    private fun File.readExifOrientation(): Int = inputStream().use { stream -> stream.readNBytes(EXIF_SCAN_BYTES) }.readExifOrientation()

    // JPEG는 투명도를 담지 못해, 투명한 부분이 있는 이미지는 불투명한 이미지에 옮겨 그린다.
    private fun BufferedImage.toOpaque(): BufferedImage {
        if (type == BufferedImage.TYPE_INT_RGB) return this

        val opaque = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        opaque.createGraphics().apply {
            drawImage(this@toOpaque, 0, 0, null)
            dispose()
        }

        return opaque
    }

    private fun BufferedImage.writeJpeg(file: File) {
        val writers = ImageIO.getImageWritersByFormatName(JPEG_FORMAT_NAME)

        check(writers.hasNext()) { "Jpeg writer is not available." }

        val writer = writers.next()
        val writeParam =
            writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = JPEG_QUALITY_PERCENT / PERCENT
            }

        ImageIO.createImageOutputStream(file).use { output ->
            writer.output = output
            writer.write(null, IIOImage(this, null, null), writeParam)
        }
        writer.dispose()
    }
}
