package io.github.taetae98coding.diary.core.file.impl.image

import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import javax.imageio.ImageIO

class JvmImageConverterTest :
    FunSpec({
        test("TC-PROFILE-IMAGE-DOMAIN-002 투명한 부분이 있는 이미지도 같은 크기의 JPEG로 바꾼다") {
            val converter = JvmImageConverter(dispatcher = Dispatchers.Default)

            val decoded =
                converter.convert(source = imageFileUri(bytes = transparentPng()), format = ImageFormat.JPEG, cropRegion = ImageCropRegion.FULL, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY).useBytes { bytes ->
                    readJpeg(bytes = bytes)
                }

            decoded.width shouldBe IMAGE_SIZE
            decoded.height shouldBe IMAGE_SIZE
            decoded.colorModel.hasAlpha() shouldBe false
        }

        test("바꾼 이미지는 원본과 다른 새 파일 위치로 알린다") {
            val converter = JvmImageConverter(dispatcher = Dispatchers.Default)
            val source = imageFileUri(bytes = transparentPng())

            val converted = converter.convert(source = source, format = ImageFormat.JPEG, cropRegion = ImageCropRegion.FULL, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY)

            converted shouldNotBe source
            converted.toFile().exists() shouldBe true
            converted.toFile().delete()
        }

        test("이미지로 읽을 수 없는 내용은 바꾸지 못하고 실패한다") {
            val converter = JvmImageConverter(dispatcher = Dispatchers.Default)

            shouldThrowAny {
                converter.convert(source = imageFileUri(bytes = "not an image".encodeToByteArray()), format = ImageFormat.JPEG, cropRegion = ImageCropRegion.FULL, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY)
            }
        }

        test("EXIF 방향 태그를 읽는다") {
            val orientation = 6

            jpegWithExifOrientation(orientation).readExifOrientation() shouldBe orientation
        }

        test("EXIF 방향 태그가 없으면 기본 방향으로 읽는다") {
            opaqueJpeg().readExifOrientation() shouldBe EXIF_ORIENTATION_NORMAL
        }

        test("EXIF 방향은 촬영 방향으로 되돌린 이미지를 만든다") {
            val image = BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB)
            image.setRGB(0, 0, Color.RED.rgb)
            image.setRGB(1, 0, Color.BLUE.rgb)

            val oriented = image.applyExifOrientation(orientation = 6)

            oriented.width shouldBe 1
            oriented.height shouldBe 2
            oriented.getRGB(0, 0) shouldBe Color.RED.rgb
            oriented.getRGB(0, 1) shouldBe Color.BLUE.rgb
        }

        test("TC-PROFILE-IMAGE-DOMAIN-002 정한 영역만 남긴 정사각형 JPEG로 바꾼다") {
            val converter = JvmImageConverter(dispatcher = Dispatchers.Default)
            val region = ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F)

            val decoded =
                converter.convert(source = imageFileUri(bytes = opaqueJpeg(width = 8, height = 4)), format = ImageFormat.JPEG, cropRegion = region, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY).useBytes { bytes ->
                    readJpeg(bytes = bytes)
                }

            decoded.width shouldBe 4
            decoded.height shouldBe 4
        }

        test("TC-PROFILE-IMAGE-DOMAIN-002 남긴 이미지의 긴 변이 최대 변 길이를 넘으면 그 길이로 줄인다") {
            val converter = JvmImageConverter(dispatcher = Dispatchers.Default)

            val decoded =
                converter.convert(source = imageFileUri(bytes = opaqueJpeg(width = 8, height = 8)), format = ImageFormat.JPEG, cropRegion = ImageCropRegion.FULL, maxSideLength = 4, quality = QUALITY).useBytes { bytes ->
                    readJpeg(bytes = bytes)
                }

            decoded.width shouldBe 4
            decoded.height shouldBe 4
        }

        test("TC-PROFILE-IMAGE-DOMAIN-002 남긴 이미지가 최대 변 길이를 넘지 않으면 해상도를 그대로 둔다") {
            val converter = JvmImageConverter(dispatcher = Dispatchers.Default)

            val decoded =
                converter.convert(source = imageFileUri(bytes = opaqueJpeg(width = 4, height = 4)), format = ImageFormat.JPEG, cropRegion = ImageCropRegion.FULL, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY).useBytes { bytes ->
                    readJpeg(bytes = bytes)
                }

            decoded.width shouldBe 4
            decoded.height shouldBe 4
        }

        test("TC-PROFILE-IMAGE-DOMAIN-002 촬영 방향을 반영한 사진에서 영역을 남긴다") {
            val converter = JvmImageConverter(dispatcher = Dispatchers.Default)
            // 8x4 사진에 90도 회전 방향이 있으면 4x8로 보이므로, 가로 전체와 세로 가운데를 남기면 4x4가 된다.
            val region = ImageCropRegion(left = 0F, top = 0.25F, right = 1F, bottom = 0.75F)

            val decoded =
                converter
                    .convert(source = imageFileUri(bytes = jpegWithExifOrientation(orientation = 6, width = 8, height = 4)), format = ImageFormat.JPEG, cropRegion = region, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY)
                    .useBytes { bytes -> readJpeg(bytes = bytes) }

            decoded.width shouldBe 4
            decoded.height shouldBe 4
        }
    }) {
    public companion object {
        private const val IMAGE_SIZE = 4
        private const val MAX_SIDE_LENGTH = 1024
        private const val QUALITY = 90
        private val JPEG_SOI_MARKER = byteArrayOf(0xFF.toByte(), 0xD8.toByte())

        private fun FileUri.toFile(): File = File(URI(value))

        // 바꾼 파일은 호출자가 지우는 계약이라 읽은 뒤 바로 지운다.
        private inline fun <T> FileUri.useBytes(block: (ByteArray) -> T): T {
            val file = toFile()

            return try {
                block(file.readBytes())
            } finally {
                file.delete()
            }
        }

        private fun readJpeg(bytes: ByteArray): BufferedImage {
            bytes.copyOfRange(0, 2) shouldBe JPEG_SOI_MARKER
            ImageIO.createImageInputStream(ByteArrayInputStream(bytes)).use { input ->
                ImageIO
                    .getImageReaders(input)
                    .next()
                    .formatName
                    .lowercase() shouldBe "jpeg"
            }

            return ImageIO.read(ByteArrayInputStream(bytes)).shouldNotBeNull()
        }

        private fun imageFileUri(bytes: ByteArray): FileUri {
            val file = File.createTempFile("profile-image-test", null)

            file.deleteOnExit()
            file.writeBytes(bytes)

            return FileUri(file.toURI().toString())
        }

        private fun transparentPng(): ByteArray {
            val image = BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB)

            image.setRGB(0, 0, Color.RED.rgb)

            return image.encode(format = "png")
        }

        private fun opaqueJpeg(
            width: Int = IMAGE_SIZE,
            height: Int = IMAGE_SIZE,
        ): ByteArray = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).encode(format = "jpg")

        // ImageIO는 EXIF를 쓰지 못해, 방향 태그만 담은 APP1 조각을 SOI 뒤에 직접 끼워 넣는다.
        private fun jpegWithExifOrientation(
            orientation: Int,
            width: Int = IMAGE_SIZE,
            height: Int = IMAGE_SIZE,
        ): ByteArray {
            val tiff =
                byteArrayOf(
                    0x4D,
                    0x4D,
                    0x00,
                    0x2A,
                    0x00,
                    0x00,
                    0x00,
                    0x08,
                    0x00,
                    0x01,
                    0x01,
                    0x12,
                    0x00,
                    0x03,
                    0x00,
                    0x00,
                    0x00,
                    0x01,
                    0x00,
                    orientation.toByte(),
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                    0x00,
                )
            val exifHeader = byteArrayOf(0x45, 0x78, 0x69, 0x66, 0x00, 0x00)
            val segmentLength = 2 + exifHeader.size + tiff.size
            val segment =
                byteArrayOf(0xFF.toByte(), 0xE1.toByte(), (segmentLength shr 8).toByte(), segmentLength.toByte()) +
                    exifHeader +
                    tiff
            val jpeg = opaqueJpeg(width = width, height = height)

            return jpeg.copyOfRange(0, 2) + segment + jpeg.copyOfRange(2, jpeg.size)
        }

        private fun BufferedImage.encode(format: String): ByteArray =
            ByteArrayOutputStream().use { stream ->
                ImageIO.write(this, format, stream)
                stream.toByteArray()
            }
    }
}
