package io.github.taetae98coding.diary.compose.map.google

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.web.WebMap
import io.github.taetae98coding.diary.compose.map.web.WebMapStateFactory
import kotlin.uuid.Uuid

@Composable
internal actual fun GoogleMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    WebMap(
        createOptions = ::googleMapOptions,
        createMapState = WebMapStateFactory(::googleMapState),
        modifier = modifier,
        state = state,
        onSpotClick = onSpotClick,
        onPinClick = onPinClick,
    )
}
