package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.taetae98coding.diary.compose.core.graphics.toUIImage
import platform.UIKit.UIImage
import kotlin.math.ceil
import kotlin.math.roundToInt

internal class PinMarkerImage(
    val image: UIImage,
    // 마커의 아래 꼭지점이 이미지 높이에서 차지하는 비율이다. 라벨이 있으면 마커 아래에 라벨이 붙어 1보다 작다.
    val anchorY: Double,
)

// 라벨을 UIKit 뷰로 겹쳐 붙이지 않고, 마커와 라벨을 Compose로 한 이미지에 그려 지도 SDK에는 이미지만 넘긴다.
internal fun createPinMarkerImage(
    color: Color,
    label: String,
    density: Float,
    textMeasurer: TextMeasurer,
): PinMarkerImage? {
    val bitmapDensity = Density(density = density)
    val pinSizePx = with(bitmapDensity) { DiaryMapPinMarkerDefaults.Size.roundToPx() }

    return drawPinMarkerBitmap(color = color, label = label, density = bitmapDensity, textMeasurer = textMeasurer)?.let { bitmap ->
        bitmap.toUIImage(scale = density.toDouble())?.let { image ->
            PinMarkerImage(image = image, anchorY = pinSizePx.toDouble() / bitmap.height)
        }
    }
}

private fun drawPinMarkerBitmap(
    color: Color,
    label: String,
    density: Density,
    textMeasurer: TextMeasurer,
): ImageBitmap? {
    return with(density) {
        val pinSizePx = DiaryMapPinMarkerDefaults.Size.roundToPx()
        if (pinSizePx <= 0) return null

        val labelLayout =
            label.takeIf { text -> text.isNotEmpty() }?.let { text ->
                textMeasurer.measure(
                    text = text,
                    style =
                        TextStyle(
                            color = Color.Black,
                            fontSize = DiaryMapPinMarkerDefaults.LabelFontSize,
                            lineHeight = DiaryMapPinMarkerDefaults.LabelLineHeight,
                        ),
                    softWrap = false,
                    maxLines = 1,
                    density = density,
                )
            }
        val labelPaddingPx = DiaryMapPinMarkerDefaults.LabelHorizontalPadding.toPx()
        val labelWidthPx = labelLayout?.let { layout -> ceil(layout.size.width + labelPaddingPx * 2).toInt() } ?: 0
        val labelHeightPx = if (labelLayout == null) 0 else DiaryMapPinMarkerDefaults.LabelHeight.roundToPx()
        val width = maxOf(pinSizePx, labelWidthPx)
        val height = pinSizePx + labelHeightPx
        val bitmap = ImageBitmap(width = width, height = height)

        CanvasDrawScope().draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(bitmap),
            size = Size(width = width.toFloat(), height = height.toFloat()),
        ) {
            translate(left = (width - pinSizePx) / 2F) {
                scale(scale = pinSizePx / PIN_MARKER_VIEWPORT_SIZE, pivot = Offset.Zero) {
                    drawPath(
                        path = PathParser().parsePathString(PIN_MARKER_PATH_DATA).toPath(),
                        color = color,
                    )
                }
            }

            if (labelLayout != null) {
                val labelLeft = (width - labelWidthPx) / 2F

                drawRoundRect(
                    color = Color.White.copy(alpha = DiaryMapPinMarkerDefaults.LABEL_BACKGROUND_ALPHA),
                    topLeft = Offset(x = labelLeft, y = pinSizePx.toFloat()),
                    size = Size(width = labelWidthPx.toFloat(), height = labelHeightPx.toFloat()),
                    cornerRadius = CornerRadius(DiaryMapPinMarkerDefaults.LabelCornerRadius.toPx()),
                )
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft =
                        Offset(
                            x = labelLeft + labelPaddingPx,
                            y = pinSizePx + (labelHeightPx - labelLayout.size.height) / 2F,
                        ),
                )
            }
        }

        bitmap
    }
}
