package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.uuid.Uuid

@Composable
internal actual fun NaverMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    MapWebView(
        state = state,
        onSpotClick = onSpotClick,
        onPinClick = onPinClick,
        modifier = modifier,
    ) {
        NaverMapHttpServer.start(
            camera = state.camera,
            spot = state.spot,
            isSpotSelectable = onSpotClick != null,
            pins = state.pins,
            isPinSelectable = onPinClick != null,
        )
    }
}
