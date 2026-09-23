package io.github.taetae98coding.diary.compose.core.image

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.DecodeUtils
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.request.maxBitmapSize
import coil3.size.Precision
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedOrigin
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.Managed

private const val SINGLE_FRAME_COUNT = 1

/**
 * Skia의 `Image.makeFromEncoded`는 EXIF 방향을 무시해 세워 찍은 사진이 누운 채로 그려진다.
 * 방향 태그가 있는 이미지만 여기서 방향을 되돌려 디코딩하고, 나머지는 기본 디코더에 맡긴다.
 */
internal class ExifOrientedSkiaImageDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        // 기본 디코더에 넘길 수 있도록 소스를 소비하거나 닫지 않고 미리 보기로 읽는다.
        val bytes = source.source().peek().readByteArray()
        val codec = runCatching { Codec.makeFromData(Data.makeFromBytes(bytes)) }.getOrElse { return null }

        return codec.useManaged { decodeOriented(codec = codec) }
    }

    private fun decodeOriented(codec: Codec): DecodeResult? {
        val origin = codec.encodedOrigin

        // 방향이 기본이거나 여러 장면이 이어지는 이미지는 기본 디코더가 그대로 다룬다.
        if (origin == EncodedOrigin.TOP_LEFT || codec.frameCount != SINGLE_FRAME_COUNT) return null

        val raw = codec.readPixels()
        val orientedWidth = if (origin.swapsWidthHeight()) raw.height else raw.width
        val orientedHeight = if (origin.swapsWidthHeight()) raw.width else raw.height
        val multiplier = sizeMultiplier(srcWidth = orientedWidth, srcHeight = orientedHeight)
        val bitmap = Bitmap()

        check(bitmap.allocN32Pixels((orientedWidth * multiplier).toInt().coerceAtLeast(1), (orientedHeight * multiplier).toInt().coerceAtLeast(1))) {
            "Bitmap allocation failed. width=$orientedWidth, height=$orientedHeight"
        }
        Image.makeFromBitmap(raw).useManaged { image ->
            Canvas(bitmap).useManaged { canvas ->
                canvas.scale(multiplier.toFloat(), multiplier.toFloat())
                canvas.concat(origin.toMatrix(orientedWidth, orientedHeight))
                canvas.drawImage(image, 0F, 0F)
            }
        }
        raw.close()
        bitmap.setImmutable()

        return DecodeResult(image = bitmap.asImage(), isSampled = multiplier < 1.0)
    }

    private fun sizeMultiplier(
        srcWidth: Int,
        srcHeight: Int,
    ): Double {
        val dstSize =
            DecodeUtils.computeDstSize(
                srcWidth = srcWidth,
                srcHeight = srcHeight,
                targetSize = options.size,
                scale = options.scale,
                maxSize = options.maxBitmapSize,
            )
        val multiplier =
            DecodeUtils.computeSizeMultiplier(
                srcWidth = srcWidth,
                srcHeight = srcHeight,
                dstWidth = dstSize.first,
                dstHeight = dstSize.second,
                scale = options.scale,
                maxSize = options.maxBitmapSize,
            )

        // 기본 디코더와 같이 정확한 크기를 요구할 때만 키운다.
        return if (options.precision == Precision.INEXACT) multiplier.coerceAtMost(1.0) else multiplier
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder = ExifOrientedSkiaImageDecoder(source = result.source, options = options)
    }
}

// Skia 객체는 wasm에서 AutoCloseable이 아니어서 stdlib의 use를 쓸 수 없다.
private inline fun <T : Managed, R> T.useManaged(block: (T) -> R): R =
    try {
        block(this)
    } finally {
        close()
    }
