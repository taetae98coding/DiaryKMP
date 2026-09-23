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
        // 목록 모드에서는 지도를 쓰지 않으므로, 지도 모드로 바꿀 때만 현재 위치를 확인한다.
        snapshotFlow { state.viewMode }
            .filter { viewMode -> viewMode == TagDetailPlaceViewMode.MAP }
            .collect { mapViewModel.fetchCurrentLocation() }
    }
}
