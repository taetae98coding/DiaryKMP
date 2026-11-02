package io.github.taetae98coding.diary.feature.account.ui.profile

internal sealed interface ProfileAccountUiState {
    data object Loading : ProfileAccountUiState

    data object Guest : ProfileAccountUiState

    data object User : ProfileAccountUiState
}
