package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.core.model.account.Account

internal sealed interface AppPeriodicSyncUiState {
    data object Loading : AppPeriodicSyncUiState

    data class Confirmed(
        val account: Account,
    ) : AppPeriodicSyncUiState
}
