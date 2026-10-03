package io.github.taetae98coding.diary.app.shared

import kotlin.uuid.Uuid

internal sealed interface AppSyncUiState {
    data object Loading : AppSyncUiState

    data object Unauthenticated : AppSyncUiState

    data class Authenticated(
        val accountId: Uuid,
    ) : AppSyncUiState
}
