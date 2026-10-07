@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.compose.core.icon.Article
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.icon.MoreHoriz
import io.github.taetae98coding.diary.compose.core.icon.Repeat
import io.github.taetae98coding.diary.compose.core.icon.Tag
import io.github.taetae98coding.diary.compose.core.icon.Today
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
import platform.UIKit.UIImageRenderingMode
import kotlin.math.roundToInt

// UITabBarItem은 UIImage만 받으므로 다른 플랫폼과 같은 벡터를 비트맵으로 그려 넘긴다.
// template으로 넘겨 선택 색과 Liquid Glass의 대비 처리는 시스템이 칠하게 한다.
@Composable
internal fun rememberTopLevelTabBarImage(topLevelNavigation: TopLevelNavigation): UIImage? {
    val painter = rememberVectorPainter(topLevelNavigation.imageVector)
    val density = LocalDensity.current

    return remember(painter, density) {
        val sizePx = with(density) { TopLevelNavigationDefaults.DestinationIconSize.toPx() }.roundToInt()
        val bitmap = ImageBitmap(width = sizePx, height = sizePx)

        CanvasDrawScope().draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(bitmap),
            size = Size(width = sizePx.toFloat(), height = sizePx.toFloat()),
        ) {
            with(painter) { draw(size = size) }
        }

        bitmap
            .toUIImage(scale = density.density.toDouble())
            ?.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
    }
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

// TopLevelNavigationIcon의 XxxIcon과 같은 심벌을 고른다.
private val TopLevelNavigation.imageVector: ImageVector
    get() =
        when (this) {
            TopLevelNavigation.Memo -> DiaryIcons.Article
            TopLevelNavigation.Tag -> DiaryIcons.Tag
            TopLevelNavigation.Calendar -> DiaryIcons.Today
            TopLevelNavigation.Routine -> DiaryIcons.Repeat
            TopLevelNavigation.More -> DiaryIcons.MoreHoriz
        }
