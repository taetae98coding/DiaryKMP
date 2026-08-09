package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toCoordinateBounds

@Composable
internal fun PlaceHomeMoveMapEffect(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    mapState: DiaryMapState = rememberDiaryMapState(),
) {
    LaunchedEffect(mapState, onEvent) {
        snapshotFlow { mapState.bounds }
            .collect { bounds ->
                onEvent(PlaceHomeScaffoldEvent.MoveMap(bounds = bounds?.toCoordinateBounds()))
            }
    }
}
