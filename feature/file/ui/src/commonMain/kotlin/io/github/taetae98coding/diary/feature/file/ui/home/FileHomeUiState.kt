package io.github.taetae98coding.diary.feature.file.ui.home

internal sealed interface FileHomeUiState {
    data object Loading : FileHomeUiState

    data object Guest : FileHomeUiState

    data object User : FileHomeUiState
}
