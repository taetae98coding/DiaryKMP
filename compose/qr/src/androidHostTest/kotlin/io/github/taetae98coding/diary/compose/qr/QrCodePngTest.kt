package io.github.taetae98coding.diary.compose.qr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.assertions.withClue
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QrCodePngTest {
    @Test
    fun `TC-5ff8fed9-81e7-4b57-992f-bc421e8bc340 공유 이미지는 QR을 흰 바탕에 네 칸 이상의 여백을 두고 그린다`() {
        listOf(
            Arb.alphanumericString(prefix = "Qr").next(),
            "WIFI:T:WPA;S:${Arb.alphanumericString(prefix = "Ssid").next()};P:${Arb.alphanumericString(prefix = "Password").next()};;",
            "a".repeat(MAX_BYTE_LENGTH),
        ).forEach { value ->
            withClue(value.take(CLUE_LENGTH)) {
                val png = qrCodePngOf(value = value)
                val bitmap = BitmapFactory.decodeByteArray(png, 0, png.size).shouldNotBeNull()
                val matrix = qrCodeEncoder.encode(value)
                val moduleSize = bitmap.width / (matrix.width + QUIET_ZONE_MODULE_COUNT * 2)
                val origin = (bitmap.width - moduleSize * matrix.width) / 2

                png.take(PNG_SIGNATURE.size) shouldBe PNG_SIGNATURE
                bitmap.width shouldBe bitmap.height
                origin shouldBeGreaterThanOrEqual moduleSize * QUIET_ZONE_MODULE_COUNT
                bitmap.assertQuietZone(origin = origin, codeSize = moduleSize * matrix.width)
                for (y in 0 until matrix.height) {
                    for (x in 0 until matrix.width) {
                        val center = bitmap.getPixel(origin + x * moduleSize + moduleSize / 2, origin + y * moduleSize + moduleSize / 2)

                        withClue("($x, $y)") { center shouldBe if (matrix[x, y]) Color.BLACK else Color.WHITE }
                    }
                }
            }
        }
    }

    private fun Bitmap.assertQuietZone(
        origin: Int,
        codeSize: Int,
    ) {
        val codeRange = origin until origin + codeSize
        val nonWhiteList =
            (0 until height).flatMap { y ->
                (0 until width)
                    .filter { x -> !(x in codeRange && y in codeRange) && getPixel(x, y) != Color.WHITE }
                    .map { x -> x to y }
            }

        nonWhiteList shouldBe emptyList()
    }

    private companion object {
        const val QUIET_ZONE_MODULE_COUNT = 4
        const val MAX_BYTE_LENGTH = 2953
        const val CLUE_LENGTH = 40
        val PNG_SIGNATURE: List<Byte> = listOf(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).map(Int::toByte)
    }
}
