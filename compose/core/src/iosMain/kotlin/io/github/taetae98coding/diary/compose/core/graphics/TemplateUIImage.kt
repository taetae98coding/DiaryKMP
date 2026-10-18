package io.github.taetae98coding.diary.compose.core.graphics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode

// UIKit·SwiftUI는 UIImage만 받으므로 다른 플랫폼과 같은 벡터 아이콘을 비트맵으로 그려 넘긴다.
// 색은 받는 쪽이 상태와 Liquid Glass 대비에 맞춰 칠하도록 모양만 담은 템플릿 이미지로 만든다.
@Composable
public fun rememberTemplateUIImage(
    imageVector: ImageVector,
    size: Dp,
): UIImage? {
    val painter = rememberVectorPainter(image = imageVector)
    val density = LocalDensity.current

    return remember(painter, size, density) { painter.toTemplateUIImage(size = size, density = density) }
}

private fun VectorPainter.toTemplateUIImage(
    size: Dp,
    density: Density,
): UIImage? {
    val sizePx = with(density) { size.roundToPx() }
    if (sizePx <= 0) return null

    val bitmap = ImageBitmap(width = sizePx, height = sizePx)

    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(bitmap),
        size = Size(width = sizePx.toFloat(), height = sizePx.toFloat()),
    ) {
        draw(size = this.size)
    }

    return bitmap
        .toUIImage(scale = density.density.toDouble())
        ?.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
}
