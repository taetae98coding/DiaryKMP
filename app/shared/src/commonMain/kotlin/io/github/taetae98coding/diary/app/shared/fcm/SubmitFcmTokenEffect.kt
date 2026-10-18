package io.github.taetae98coding.diary.app.shared.fcm

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.app.shared.AppFcmTokenUiState
import io.github.taetae98coding.diary.app.shared.syncMinActiveState
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.core.model.account.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun SubmitFcmTokenEffect(
    submit: (Account) -> Unit,
    uiState: Flow<AppFcmTokenUiState> = emptyFlow(),
) {
    CollectEffect(
        effect = uiState,
        minActiveState = syncMinActiveState,
    ) { value ->
        if (value is AppFcmTokenUiState.Confirmed) submit(value.account)
    }
}
