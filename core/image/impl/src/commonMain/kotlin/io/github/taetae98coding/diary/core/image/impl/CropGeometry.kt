package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import kotlin.math.roundToInt

internal data class PixelRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
) {
    val right: Int get() = left + width

    val bottom: Int get() = top + height

    val longestSide: Int get() = maxOf(width, height)
}

internal data class PixelSize(
    val width: Int,
    val height: Int,
) {
    val longestSide: Int get() = maxOf(width, height)
}

// 비율을 화소로 옮길 때 반올림으로 폭이 0이 되면 자를 수 없으므로 최소 한 화소는 남긴다.
internal fun ImageCropRegion.toPixelRect(
    imageWidth: Int,
    imageHeight: Int,
): PixelRect {
    val left = (left * imageWidth).roundToInt().coerceIn(0, imageWidth - 1)
    val top = (top * imageHeight).roundToInt().coerceIn(0, imageHeight - 1)
    val right = (right * imageWidth).roundToInt().coerceIn(left + 1, imageWidth)
    val bottom = (bottom * imageHeight).roundToInt().coerceIn(top + 1, imageHeight)

    return PixelRect(left = left, top = top, width = right - left, height = bottom - top)
}

// 긴 변이 최대 변 길이를 넘을 때만 줄이고, 넘지 않으면 늘리지 않고 그대로 둔다.
internal fun PixelRect.scaleToFit(maxSideLength: Int): Double = minOf(1.0, maxSideLength.toDouble() / longestSide)

internal fun PixelRect.scaled(scale: Double): PixelSize =
    PixelSize(
        width = (width * scale).roundToInt().coerceAtLeast(1),
        height = (height * scale).roundToInt().coerceAtLeast(1),
    )

internal fun PixelSize.scaled(scale: Double): PixelSize =
    PixelSize(
        width = (width * scale).roundToInt().coerceAtLeast(1),
        height = (height * scale).roundToInt().coerceAtLeast(1),
    )
