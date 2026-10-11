package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import kotlin.uuid.Uuid

@Composable
internal fun WebMap(
    createHtml: (
        camera: DiaryMapCamera?,
        spot: DiaryMapCoordinate?,
        isSpotSelectable: Boolean,
        pins: List<DiaryMapPin>,
        isPinSelectable: Boolean,
    ) -> String?,
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    MapWebView(
        createHtml = {
            createHtml(
                state.camera,
                state.spot,
                onSpotClick != null,
                state.pins,
                onPinClick != null,
            )
        },
        modifier = modifier,
        state = state,
        onSpotClick = onSpotClick,
        onPinClick = onPinClick,
    )
}
