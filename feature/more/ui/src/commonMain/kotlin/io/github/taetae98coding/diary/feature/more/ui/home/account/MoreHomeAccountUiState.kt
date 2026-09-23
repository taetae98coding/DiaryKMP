package io.github.taetae98coding.diary.feature.more.ui.home.account

internal sealed interface MoreHomeAccountUiState {
    data object Loading : MoreHomeAccountUiState

    data object Guest : MoreHomeAccountUiState

    data class User(
        val profileImage: String?,
        val email: String,
    ) : MoreHomeAccountUiState
}
