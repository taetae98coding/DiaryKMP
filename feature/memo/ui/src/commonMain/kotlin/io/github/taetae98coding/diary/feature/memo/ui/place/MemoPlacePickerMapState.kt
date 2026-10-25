package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState

@Composable
internal fun rememberMemoPlacePickerMapState(cardMapState: DiaryMapState): DiaryMapState =
    key(cardMapState.provider) {
        rememberDiaryMapState(
            initialProvider = cardMapState.provider,
            initialCoordinate = cardMapState.coordinate,
        )
    }
