package io.github.taetae98coding.diary.feature.memo.ui.place

internal data class MemoPlaceCardUiState(
    val mapUiState: MemoPlaceMapUiState = MemoPlaceMapUiState.Loading,
    val placeUiState: MemoPlaceInputUiState = MemoPlaceInputUiState(),
)
