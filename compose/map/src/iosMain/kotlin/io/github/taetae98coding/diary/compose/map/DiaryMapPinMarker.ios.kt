@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import kotlin.math.roundToInt

internal fun createPinMarkerImage(
    color: Color,
    density: Float,
): UIImage? {
    val sizePx = (PIN_MARKER_SIZE_DP * density).roundToInt()
    if (sizePx <= 0) return null

    val bitmap = ImageBitmap(width = sizePx, height = sizePx)

    CanvasDrawScope().draw(
        density = Density(density = density),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(bitmap),
        size = Size(width = sizePx.toFloat(), height = sizePx.toFloat()),
    ) {
        scale(scale = sizePx / PIN_MARKER_VIEWPORT_SIZE, pivot = Offset.Zero) {
            drawPath(
                path = PathParser().parsePathString(PIN_MARKER_PATH_DATA).toPath(),
                color = color,
            )
        }
    }

    return bitmap.toUIImage(scale = density.toDouble())
}

private fun ImageBitmap.toUIImage(scale: Double): UIImage? {
    val bytes =
        Image
            .makeFromBitmap(asSkiaBitmap())
            .encodeToData(EncodedImageFormat.PNG)
            ?.bytes
            ?.takeIf { encoded -> encoded.isNotEmpty() }
            ?: return null

    val nsData =
        bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.convert())
        }

    return UIImage(data = nsData, scale = scale)
}
