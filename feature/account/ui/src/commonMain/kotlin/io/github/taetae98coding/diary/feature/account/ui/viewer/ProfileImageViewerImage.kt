package io.github.taetae98coding.diary.feature.account.ui.viewer

import androidx.compose.animation.core.animate
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.BrokenImageIcon
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.account.ui.Res
import io.github.taetae98coding.diary.feature.account.ui.profile_image_content_description
import io.github.taetae98coding.diary.feature.account.ui.profile_image_viewer_failed_message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.exp

@Composable
internal fun ProfileImageViewerImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    zoomState: ProfileImageViewerZoomState = rememberProfileImageViewerZoomState(),
) {
    val painter = rememberAsyncImagePainter(model = imageUrl)
    val painterState by painter.state.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        when (painterState) {
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Loading,
            -> DiaryLoadingBox(modifier = Modifier.fillMaxSize())

            is AsyncImagePainter.State.Error -> {
                DiaryEmptyBox(
                    title = stringResource(Res.string.profile_image_viewer_failed_message),
                    modifier = Modifier.fillMaxSize(),
                    icon = { BrokenImageIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }

            is AsyncImagePainter.State.Success -> {
                ZoomableImage(
                    painter = painter,
                    zoomState = zoomState,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    painter: AsyncImagePainter,
    zoomState: ProfileImageViewerZoomState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    Image(
        painter = painter,
        contentDescription = stringResource(Res.string.profile_image_content_description),
        modifier =
            modifier
                .onSizeChanged { size -> zoomState.updateSize(containerSize = size.toSize(), imageSize = painter.intrinsicSize) }
                .pointerInput(zoomState) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        zoomState.transform(centroid = centroid - size.toSize().center(), pan = pan, zoom = zoom)
                    }
                }.pointerInput(zoomState) {
                    detectTapGestures(
                        onDoubleTap = { position ->
                            coroutineScope.toggleZoom(zoomState = zoomState, focal = position - size.toSize().center())
                        },
                    )
                }.pointerInput(zoomState) {
                    awaitEachGesture {
                        val event = awaitPointerEvent()

                        if (event.type == PointerEventType.Scroll) {
                            val change = event.changes.first()

                            zoomState.transform(
                                centroid = change.position - size.toSize().center(),
                                pan = Offset.Zero,
                                zoom = exp(-change.scrollDelta.y * ProfileImageViewerDefaults.SCROLL_ZOOM_SENSITIVITY),
                            )
                            change.consume()
                        }
                    }
                }.graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offset.x
                    translationY = zoomState.offset.y
                },
        contentScale = ContentScale.Fit,
    )
}

private fun Size.center(): Offset = Offset(x = width / 2, y = height / 2)

private fun CoroutineScope.toggleZoom(
    zoomState: ProfileImageViewerZoomState,
    focal: Offset,
) {
    val startScale = zoomState.scale
    val startOffset = zoomState.offset
    val isZoomedIn = startScale > ProfileImageViewerZoomState.MIN_SCALE
    val targetScale = if (isZoomedIn) ProfileImageViewerZoomState.MIN_SCALE else ProfileImageViewerDefaults.DOUBLE_TAP_SCALE
    val targetOffset = if (isZoomedIn) Offset.Zero else -focal * (targetScale - 1F)

    launch {
        animate(initialValue = 0F, targetValue = 1F) { fraction, _ ->
            zoomState.snapTo(
                scale = startScale + (targetScale - startScale) * fraction,
                offset = startOffset + (targetOffset - startOffset) * fraction,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun ProfileImageViewerImagePreview() {
    DiaryTheme {
        ProfileImageViewerImage(imageUrl = "https://example.com/profile.jpg")
    }
}
