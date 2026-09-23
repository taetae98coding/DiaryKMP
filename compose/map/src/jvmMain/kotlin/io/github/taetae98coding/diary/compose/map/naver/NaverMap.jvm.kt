package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.web.MapWebView
import kotlin.uuid.Uuid

@Composable
internal actual fun NaverMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    MapWebView(
        startHttpServer = {
            NaverMapHttpServer.start(
                camera = state.camera,
                spot = state.spot,
                isSpotSelectable = onSpotClick != null,
                pins = state.pins,
                isPinSelectable = onPinClick != null,
            )
        },
        modifier = modifier,
        state = state,
        onSpotClick = onSpotClick,
        onPinClick = onPinClick,
    )
}
