package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailPlaceTargetHost(
    id: Uuid,
    placeMapViewModel: TagDetailPlaceMapViewModel,
    content: @Composable (placeState: TagDetailPlaceState, placeMapState: DiaryMapState) -> Unit,
) {
    val placeState = rememberTagDetailPlaceState()
    val placeMapUiState by placeMapViewModel.uiState.collectAsStateWithLifecycle()
    val placeMapState = rememberTagDetailPlaceMapState(uiState = placeMapUiState)

    key(id) {
        content(placeState, placeMapState)
    }
}
