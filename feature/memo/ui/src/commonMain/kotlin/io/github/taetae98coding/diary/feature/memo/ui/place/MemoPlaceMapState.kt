package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate

@Composable
internal fun rememberMemoPlaceMapState(uiState: MemoPlaceCardUiState): DiaryMapState? {
    val mapUiState = uiState.mapUiState
    val placeUiState = uiState.placeUiState

    val firstPlaceCoordinate =
        placeUiState.selectedPlaceList
            .firstOrNull()
            ?.detail
            ?.coordinate

    if (
        mapUiState !is MemoPlaceMapUiState.Content ||
        !placeUiState.isSelectedPlaceLoaded ||
        !mapUiState.hasInitialCoordinate(firstPlaceCoordinate = firstPlaceCoordinate)
    ) {
        return null
    }

    val initialCoordinate = remember { firstPlaceCoordinate ?: mapUiState.currentCoordinate }

    return key(mapUiState.provider) {
        rememberDiaryMapState(
            initialProvider = mapUiState.provider.toDiaryMapProvider(),
            initialCoordinate = initialCoordinate?.toDiaryMapCoordinate(),
        )
    }
}

private fun MemoPlaceMapUiState.Content.hasInitialCoordinate(firstPlaceCoordinate: Coordinate?): Boolean = firstPlaceCoordinate != null || isCurrentLocationFetched
