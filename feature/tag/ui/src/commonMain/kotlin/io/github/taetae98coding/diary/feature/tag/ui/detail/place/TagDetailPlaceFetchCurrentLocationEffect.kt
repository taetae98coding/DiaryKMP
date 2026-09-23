package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filter

@Composable
internal fun TagDetailPlaceFetchCurrentLocationEffect(
    mapViewModel: TagDetailPlaceMapViewModel,
    state: TagDetailPlaceState = rememberTagDetailPlaceState(),
) {
    LaunchedEffect(state, mapViewModel) {
        snapshotFlow { state.viewMode }
            .filter { viewMode -> viewMode == TagDetailPlaceViewMode.MAP }
            .collect { mapViewModel.fetchCurrentLocation() }
    }
}
