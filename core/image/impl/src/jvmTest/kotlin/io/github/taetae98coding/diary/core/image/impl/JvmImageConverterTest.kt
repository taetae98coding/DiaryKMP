package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class JvmImageConverterTest :
    FunSpec({
        test("투명한 부분이 있는 이미지도 같은 크기의 JPEG로 바꾼다") {
            val converter = JvmImageConverter()

            val decoded =
                converter.toJpeg(uri = imageFileUri(bytes = transparentPng())).use { jpeg ->
                    ImageIO.read(ByteArrayInputStream(jpeg.readBytes()))
                }

            decoded.shouldNotBeNull()
            decoded.width shouldBe IMAGE_SIZE
            decoded.height shouldBe IMAGE_SIZE
            decoded.colorModel.hasAlpha() shouldBe false
        }

        test("바꾼 이미지의 크기를 알린다") {
            val converter = JvmImageConverter()

            converter.toJpeg(uri = imageFileUri(bytes = transparentPng())).use { jpeg ->
                jpeg.size shouldBe jpeg.readBytes().size.toLong()
            }
        }

        test("다 쓰고 닫으면 바꾼 이미지를 더 읽을 수 없다") {
            val converter = JvmImageConverter()

            val jpeg = converter.toJpeg(uri = imageFileUri(bytes = transparentPng()))
            jpeg.close()

            shouldThrowAny { jpeg.openSource() }
        }

        test("이미지로 읽을 수 없는 내용은 바꾸지 못하고 실패한다") {
            val converter = JvmImageConverter()

            shouldThrowAny { converter.toJpeg(uri = imageFileUri(bytes = "not an image".encodeToByteArray())) }
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
    }) {
    public companion object {
        private const val IMAGE_SIZE = 4

        private fun JpegSource.readBytes(): ByteArray = openSource().buffered().use { source -> source.readByteArray() }

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

        private fun opaqueJpeg(): ByteArray = BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB).encode(format = "jpg")

        // ImageIO는 EXIF를 쓰지 못해, 방향 태그만 담은 APP1 조각을 SOI 뒤에 직접 끼워 넣는다.
        private fun jpegWithExifOrientation(orientation: Int): ByteArray {
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
            val jpeg = opaqueJpeg()

            return jpeg.copyOfRange(0, 2) + segment + jpeg.copyOfRange(2, jpeg.size)
        }

        private fun BufferedImage.encode(format: String): ByteArray =
            ByteArrayOutputStream().use { stream ->
                ImageIO.write(this, format, stream)
                stream.toByteArray()
            }
    }
}
