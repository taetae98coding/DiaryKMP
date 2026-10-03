package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.core.model.account.Account

internal sealed interface AppFileUploadUiState {
    data object Loading : AppFileUploadUiState

    data class Confirmed(
        val account: Account,
    ) : AppFileUploadUiState
}
