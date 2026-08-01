package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapProvider

@Composable
internal fun rememberMemoPlaceMapState(uiState: MemoPlaceCardUiState): DiaryMapState? {
    val mapUiState = uiState.mapUiState
    val placeUiState = uiState.placeUiState

    if (mapUiState !is MemoPlaceMapUiState.Loaded || !placeUiState.isSelectedPlaceLoaded) return null

    val initialCoordinate =
        remember {
            placeUiState.selectedPlaceList
                .firstOrNull()
                ?.detail
                ?.coordinate ?: mapUiState.currentCoordinate
        }

    return key(mapUiState.provider) {
        rememberDiaryMapState(
            initialProvider = mapUiState.provider.toDiaryMapProvider(),
            initialCoordinate = initialCoordinate?.toDiaryMapCoordinate(),
        )
    }
}
