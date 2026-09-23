package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion

private const val URI_INDEX = 0
private const val ZOOM_INDEX = 1
private const val CENTER_X_INDEX = 2
private const val CENTER_Y_INDEX = 3
private const val HALF = 0.5F

/**
 * 남길 영역을 틀 크기와 무관하게 확대 배율과 사진 기준 비율 중심으로 들고 있어, 창이 회전하거나 크기가 바뀌어도 같은 부분이 남는다.
 */
@Stable
internal class ProfileImageEditState(
    initialUri: FileUri? = null,
    initialPhoto: ProfileImageEditPhoto = if (initialUri == null) ProfileImageEditPhoto.Empty else ProfileImageEditPhoto.Loading,
    initialZoom: Float = MIN_ZOOM,
    initialCenterX: Float = HALF,
    initialCenterY: Float = HALF,
) {
    var uri: FileUri? by mutableStateOf(initialUri)
        private set

    var photo: ProfileImageEditPhoto by mutableStateOf(initialPhoto)
        private set

    var zoom: Float by mutableFloatStateOf(initialZoom)
        private set

    var centerX: Float by mutableFloatStateOf(initialCenterX)
        private set

    var centerY: Float by mutableFloatStateOf(initialCenterY)
        private set

    val isReady: Boolean get() = photo is ProfileImageEditPhoto.Ready

    fun changePhoto(uri: FileUri) {
        this.uri = uri
        photo = ProfileImageEditPhoto.Loading
        zoom = MIN_ZOOM
        centerX = HALF
        centerY = HALF
    }

    fun onPhotoLoading() {
        photo = ProfileImageEditPhoto.Loading
    }

    fun onPhotoLoaded(
        width: Int,
        height: Int,
    ) {
        val ready = ProfileImageEditPhoto.Ready(width = width, height = height)

        photo = ready
        clampCenter(ready = ready)
    }

    fun onPhotoUnreadable() {
        photo = ProfileImageEditPhoto.Unreadable
    }

    fun cropRegion(): ImageCropRegion? = (photo as? ProfileImageEditPhoto.Ready)?.let { ready -> cropRegion(ready = ready) }

    fun cropRegion(ready: ProfileImageEditPhoto.Ready): ImageCropRegion {
        val halfWidth = ready.regionWidth(zoom) * HALF
        val halfHeight = ready.regionHeight(zoom) * HALF

        return ImageCropRegion(
            left = (centerX - halfWidth).coerceIn(0F, 1F),
            top = (centerY - halfHeight).coerceIn(0F, 1F),
            right = (centerX + halfWidth).coerceIn(0F, 1F),
            bottom = (centerY + halfHeight).coerceIn(0F, 1F),
        )
    }

    fun transform(
        pan: Offset,
        zoomChange: Float,
        centroid: Offset,
        frameSize: Size,
    ) {
        val ready = photo as? ProfileImageEditPhoto.Ready ?: return

        zoomAround(ready = ready, factor = zoomChange, focal = centroid, frameSize = frameSize)
        // 사진을 오른쪽으로 끌면 남길 영역은 사진 위에서 왼쪽으로 옮겨진다.
        centerX -= pan.x / frameSize.width * ready.regionWidth(zoom)
        centerY -= pan.y / frameSize.height * ready.regionHeight(zoom)
        clampCenter(ready = ready)
    }

    fun zoomBy(
        factor: Float,
        focal: Offset,
        frameSize: Size,
    ) {
        val ready = photo as? ProfileImageEditPhoto.Ready ?: return

        zoomAround(ready = ready, factor = factor, focal = focal, frameSize = frameSize)
        clampCenter(ready = ready)
    }

    // 초점 아래에 있던 사진의 지점이 확대 뒤에도 같은 자리에 남도록 중심을 옮긴다.
    private fun zoomAround(
        ready: ProfileImageEditPhoto.Ready,
        factor: Float,
        focal: Offset,
        frameSize: Size,
    ) {
        val newZoom = (zoom * factor).coerceIn(MIN_ZOOM, MAX_ZOOM)

        if (newZoom == zoom) return

        val focalX = focal.x / frameSize.width
        val focalY = focal.y / frameSize.height
        val width = ready.regionWidth(zoom)
        val height = ready.regionHeight(zoom)
        val newWidth = ready.regionWidth(newZoom)
        val newHeight = ready.regionHeight(newZoom)
        val pointX = centerX - width * HALF + focalX * width
        val pointY = centerY - height * HALF + focalY * height

        centerX = pointX - focalX * newWidth + newWidth * HALF
        centerY = pointY - focalY * newHeight + newHeight * HALF
        zoom = newZoom
    }

    private fun clampCenter(ready: ProfileImageEditPhoto.Ready) {
        val halfWidth = ready.regionWidth(zoom) * HALF
        val halfHeight = ready.regionHeight(zoom) * HALF

        centerX = centerX.coerceIn(halfWidth, 1F - halfWidth)
        centerY = centerY.coerceIn(halfHeight, 1F - halfHeight)
    }

    companion object {
        const val MIN_ZOOM: Float = 1F
        const val MAX_ZOOM: Float = 5F

        // 저장 목록의 요소는 null일 수 없어 사진이 없으면 빈 문자열로 저장한다.
        val Saver: Saver<ProfileImageEditState, Any> =
            listSaver(
                save = { state -> listOf(state.uri?.value.orEmpty(), state.zoom, state.centerX, state.centerY) },
                restore = { value ->
                    ProfileImageEditState(
                        initialUri = (value[URI_INDEX] as String).takeIf { uri -> uri.isNotEmpty() }?.let(::FileUri),
                        initialZoom = value[ZOOM_INDEX] as Float,
                        initialCenterX = value[CENTER_X_INDEX] as Float,
                        initialCenterY = value[CENTER_Y_INDEX] as Float,
                    )
                },
            )
    }
}

@Composable
internal fun rememberProfileImageEditState(): ProfileImageEditState =
    rememberSaveable(saver = ProfileImageEditState.Saver) {
        ProfileImageEditState()
    }
