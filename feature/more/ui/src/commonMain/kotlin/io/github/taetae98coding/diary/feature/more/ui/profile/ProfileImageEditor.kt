package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.BrokenImageIcon
import io.github.taetae98coding.diary.compose.core.icon.PhotoIcon
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.Res
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_empty_message
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_photo_content_description
import io.github.taetae98coding.diary.feature.more.ui.more_profile_image_edit_unreadable_message
import org.jetbrains.compose.resources.stringResource
import kotlin.math.exp

// 틀은 최대 400dp이고 5배까지 키우므로, 미리보기는 이 크기까지만 읽어도 흐려지지 않는다.
private const val PREVIEW_MAX_PIXEL_SIZE = 2048

@Composable
internal fun ProfileImageEditor(
    modifier: Modifier = Modifier,
    state: ProfileImageEditState = rememberProfileImageEditState(),
    isEnabledProvider: () -> Boolean = { true },
) {
    val context = LocalPlatformContext.current
    val uri = state.uri
    val painter =
        rememberAsyncImagePainter(
            model =
                remember(context, uri) {
                    uri?.let { uri ->
                        ImageRequest
                            .Builder(context)
                            .data(uri.value)
                            .size(PREVIEW_MAX_PIXEL_SIZE)
                            .build()
                    }
                },
        )

    LoadPhotoEffect(painter = painter, state = state)

    Box(modifier = modifier) {
        when (val photo = state.photo) {
            is ProfileImageEditPhoto.Empty -> {
                DiaryEmptyBox(
                    title = stringResource(Res.string.more_profile_image_edit_empty_message),
                    modifier = Modifier.fillMaxSize(),
                    icon = { PhotoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }

            is ProfileImageEditPhoto.Loading -> {
                DiaryLoadingBox(modifier = Modifier.fillMaxSize())
            }

            is ProfileImageEditPhoto.Unreadable -> {
                DiaryEmptyBox(
                    title = stringResource(Res.string.more_profile_image_edit_unreadable_message),
                    modifier = Modifier.fillMaxSize(),
                    icon = { BrokenImageIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }

            is ProfileImageEditPhoto.Ready -> {
                ProfileImageEditFrame(
                    painter = painter,
                    state = state,
                    photo = photo,
                    modifier = Modifier.fillMaxSize(),
                    isEnabledProvider = isEnabledProvider,
                )
            }
        }
    }
}

@Composable
private fun LoadPhotoEffect(
    painter: AsyncImagePainter,
    state: ProfileImageEditState,
) {
    LaunchedEffect(painter, state) {
        // Coil은 사진이 없는 null 모델도 오류로 알리므로, 고른 사진이 있을 때만 읽기 결과를 반영한다.
        painter.state.collect { value ->
            if (state.uri == null) return@collect

            when (value) {
                is AsyncImagePainter.State.Empty -> Unit

                is AsyncImagePainter.State.Loading -> state.onPhotoLoading()

                is AsyncImagePainter.State.Success -> {
                    val size = value.painter.intrinsicSize

                    state.onPhotoLoaded(width = size.width.toInt(), height = size.height.toInt())
                }

                is AsyncImagePainter.State.Error -> state.onPhotoUnreadable()
            }
        }
    }
}

@Composable
private fun ProfileImageEditFrame(
    painter: Painter,
    state: ProfileImageEditState,
    photo: ProfileImageEditPhoto.Ready,
    modifier: Modifier = Modifier,
    isEnabledProvider: () -> Boolean = { true },
) {
    val latestIsEnabledProvider by rememberUpdatedState(isEnabledProvider)
    val scrimColor = DiaryTheme.colorScheme.scrim.copy(alpha = ProfileImageEditorDefaults.SCRIM_ALPHA)
    val outlineColor = DiaryTheme.colorScheme.outline
    val contentDescription = stringResource(Res.string.more_profile_image_edit_photo_content_description)

    Box(
        modifier =
            modifier
                .semantics { this.contentDescription = contentDescription }
                .pointerInput(state, photo) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (latestIsEnabledProvider()) {
                            state.transform(pan = pan, zoomChange = zoom, centroid = centroid, frameSize = size.toSize())
                        }
                    }
                }.pointerInput(state, photo) {
                    awaitEachGesture {
                        val event = awaitPointerEvent()

                        if (event.type == PointerEventType.Scroll && latestIsEnabledProvider()) {
                            val change = event.changes.first()

                            state.zoomBy(factor = exp(-change.scrollDelta.y * ProfileImageEditorDefaults.SCROLL_ZOOM_SENSITIVITY), focal = change.position, frameSize = size.toSize())
                            change.consume()
                        }
                    }
                }.drawBehind {
                    val region = state.cropRegion(ready = photo)
                    val scale = size.width / photo.shortSide * state.zoom
                    val imageSize = Size(width = photo.width * scale, height = photo.height * scale)
                    val topLeft = Offset(x = -region.left * imageSize.width, y = -region.top * imageSize.height)

                    translate(left = topLeft.x, top = topLeft.y) {
                        with(painter) { draw(size = imageSize) }
                    }
                    drawScrim(color = scrimColor, imageTopLeft = topLeft, imageSize = imageSize)
                    inset(inset = ProfileImageEditorDefaults.FrameBorderWidth.toPx() / 2) {
                        drawRect(color = outlineColor, style = Stroke(width = ProfileImageEditorDefaults.FrameBorderWidth.toPx()))
                    }
                },
    )
}

private fun DrawScope.drawScrim(
    color: Color,
    imageTopLeft: Offset,
    imageSize: Size,
) {
    clipRect(left = 0F, top = 0F, right = size.width, bottom = size.height, clipOp = ClipOp.Difference) {
        drawRect(color = color, topLeft = imageTopLeft, size = imageSize)
    }
}

@ComponentPreview
@Composable
private fun ProfileImageEditorPreview() {
    DiaryTheme {
        Surface {
            ProfileImageEditor(modifier = Modifier.size(240.dp))
        }
    }
}
