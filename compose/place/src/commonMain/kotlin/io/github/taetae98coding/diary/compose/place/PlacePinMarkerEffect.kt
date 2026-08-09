package io.github.taetae98coding.diary.compose.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.core.model.place.Place

@Composable
public fun PlacePinMarkerEffect(
    mapState: DiaryMapState = rememberDiaryMapState(),
    placeListProvider: () -> List<Place> = { emptyList() },
) {
    val latestPlaceListProvider by rememberUpdatedState(placeListProvider)

    LaunchedEffect(mapState) {
        snapshotFlow { latestPlaceListProvider() }
            .collect { placeList ->
                mapState.updatePins(placeList.map { place -> place.toDiaryMapPin() })
            }
    }
}
