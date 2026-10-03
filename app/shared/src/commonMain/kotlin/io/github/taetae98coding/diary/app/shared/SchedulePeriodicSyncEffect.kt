package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun SchedulePeriodicSyncEffect(
    schedulePeriodicSync: () -> Unit,
    uiState: Flow<AppPeriodicSyncUiState> = emptyFlow(),
) {
    CollectEffect(
        effect = uiState,
        minActiveState = syncMinActiveState,
    ) { value ->
        if (value is AppPeriodicSyncUiState.Confirmed) schedulePeriodicSync()
    }
}
