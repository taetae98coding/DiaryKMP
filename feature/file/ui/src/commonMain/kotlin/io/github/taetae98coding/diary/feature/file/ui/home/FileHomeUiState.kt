package io.github.taetae98coding.diary.feature.file.ui.home

import kotlin.uuid.Uuid

internal sealed interface FileHomeUiState {
    data object Loading : FileHomeUiState

    data object Guest : FileHomeUiState

    data class User(
        val accountId: Uuid,
    ) : FileHomeUiState
}
