package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapProvider

@Composable
internal fun rememberPlaceHomeMapState(uiState: PlaceHomeUiState): DiaryMapState =
    when (uiState) {
        is PlaceHomeUiState.Loading -> rememberDiaryMapState()

        is PlaceHomeUiState.Loaded ->
            key(uiState.defaultProvider, uiState.initialCoordinate) {
                rememberDiaryMapState(
                    initialProvider = uiState.defaultProvider.toDiaryMapProvider(),
                    initialCoordinate = uiState.initialCoordinate?.toDiaryMapCoordinate(),
                )
            }
    }
