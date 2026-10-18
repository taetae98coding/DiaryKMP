package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun SyncEffect(
    requestSync: () -> Unit,
    uiState: Flow<AppSyncUiState> = emptyFlow(),
) {
    CollectEffect(
        effect = uiState,
        minActiveState = syncMinActiveState,
    ) { value ->
        if (value is AppSyncUiState.Authenticated) requestSync()
    }
}
