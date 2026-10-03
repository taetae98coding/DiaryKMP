package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.core.model.account.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun ReconcileFileUploadEffect(
    reconcile: (Account) -> Unit,
    uiState: Flow<AppFileUploadUiState> = emptyFlow(),
) {
    CollectEffect(effect = uiState) { value ->
        if (value is AppFileUploadUiState.Confirmed) reconcile(value.account)
    }
}
