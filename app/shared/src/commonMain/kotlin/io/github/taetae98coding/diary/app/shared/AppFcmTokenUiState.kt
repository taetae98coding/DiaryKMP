package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.core.model.account.Account

internal sealed interface AppFcmTokenUiState {
    data object Loading : AppFcmTokenUiState

    data class Confirmed(
        val account: Account,
    ) : AppFcmTokenUiState
}
