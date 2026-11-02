package io.github.taetae98coding.diary.feature.account.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.min

@Stable
internal class ProfileImageViewerZoomState {
    var scale: Float by mutableFloatStateOf(MIN_SCALE)
        private set

    var offset: Offset by mutableStateOf(Offset.Zero)
        private set

    private var containerSize: Size = Size.Zero
    private var imageSize: Size = Size.Zero

    fun updateSize(
        containerSize: Size,
        imageSize: Size,
    ) {
        this.containerSize = containerSize
        this.imageSize = imageSize
        offset = clampOffset(offset = offset, scale = scale)
    }

    // centroid는 화면 가운데를 원점으로 한 위치다. 그 자리가 확대·축소 뒤에도 같은 곳에 남도록 위치를 옮긴다.
    fun transform(
        centroid: Offset,
        pan: Offset,
        zoom: Float,
    ) {
        val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
        val factor = newScale / scale
        val newOffset = (offset - centroid) * factor + centroid + pan

        scale = newScale
        offset = clampOffset(offset = newOffset, scale = newScale)
    }

    fun snapTo(
        scale: Float,
        offset: Offset,
    ) {
        val newScale = scale.coerceIn(MIN_SCALE, MAX_SCALE)

        this.scale = newScale
        this.offset = clampOffset(offset = offset, scale = newScale)
    }

    fun fittedImageSize(): Size {
        if (imageSize.isEmpty() || containerSize.isEmpty()) return Size.Zero

        val fit = min(containerSize.width / imageSize.width, containerSize.height / imageSize.height)

        return Size(width = imageSize.width * fit, height = imageSize.height * fit)
    }

    private fun clampOffset(
        offset: Offset,
        scale: Float,
    ): Offset {
        val fitted = fittedImageSize()
        val maxX = max(0F, (fitted.width * scale - containerSize.width) / 2)
        val maxY = max(0F, (fitted.height * scale - containerSize.height) / 2)

        return Offset(x = offset.x.coerceIn(-maxX, maxX), y = offset.y.coerceIn(-maxY, maxY))
    }

    companion object {
        const val MIN_SCALE: Float = 1F
        const val MAX_SCALE: Float = 5F
    }
}

@Composable
internal fun rememberProfileImageViewerZoomState(): ProfileImageViewerZoomState = remember { ProfileImageViewerZoomState() }
