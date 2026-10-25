package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toCoordinateBounds

@Composable
internal fun MemoPlacePickerMoveMapEffect(
    onEvent: (MemoPlacePickerEvent) -> Unit,
    mapState: DiaryMapState,
) {
    val latestOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(mapState) {
        snapshotFlow { mapState.bounds }
            .collect { bounds ->
                latestOnEvent(MemoPlacePickerEvent.MoveMap(bounds = bounds?.toCoordinateBounds()))
            }
    }
}
