package io.github.taetae98coding.diary.feature.place.ui.home.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapProvider
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeUiState

@Composable
internal fun rememberPlaceHomeMapState(uiState: PlaceHomeUiState): DiaryMapState =
    when (uiState) {
        is PlaceHomeUiState.Loading -> rememberDiaryMapState()

        // 저장한 카메라는 같은 확인 결과를 다시 그릴 때만 이어받는다. 되살아나 새로 확인하면 좌표가 같아도 다른 키가 된다.
        is PlaceHomeUiState.Loaded ->
            key(uiState.defaultProvider, uiState.currentLocationFetchId) {
                rememberDiaryMapState(
                    initialProvider = uiState.defaultProvider.toDiaryMapProvider(),
                    initialCoordinate = uiState.initialCoordinate?.toDiaryMapCoordinate(),
                )
            }
    }
