package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toCoordinate
import io.github.taetae98coding.diary.compose.place.toCoordinateBounds

@Composable
internal fun TagDetailPlaceMoveMapEffect(
    onEvent: (TagDetailPlaceContentEvent) -> Unit,
    mapState: DiaryMapState = rememberDiaryMapState(),
) {
    val latestOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(mapState) {
        snapshotFlow { mapState.bounds }
            .collect { bounds ->
                latestOnEvent(
                    TagDetailPlaceContentEvent.MoveMap(
                        bounds = bounds?.toCoordinateBounds(),
                        coordinate = mapState.coordinate?.toCoordinate(),
                    ),
                )
            }
    }
}
