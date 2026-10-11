package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.web.WebMap
import kotlin.uuid.Uuid

@Composable
internal actual fun NaverMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    WebMap(
        createHtml = { camera, spot, isSpotSelectable, pins, isPinSelectable ->
            createNaverMapHtml(
                camera = camera,
                spot = spot,
                isSpotSelectable = isSpotSelectable,
                pins = pins,
                isPinSelectable = isPinSelectable,
            )
        },
        modifier = modifier,
        state = state,
        onSpotClick = onSpotClick,
        onPinClick = onPinClick,
    )
}
