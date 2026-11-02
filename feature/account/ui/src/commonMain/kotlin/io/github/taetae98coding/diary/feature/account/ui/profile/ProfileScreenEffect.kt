package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.account.ui.Res
import io.github.taetae98coding.diary.feature.account.ui.profile_delete_failed_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileGuestEffect(
    navigateUp: () -> Unit,
    accountUiState: Flow<ProfileAccountUiState> = emptyFlow(),
) {
    CollectEffect(accountUiState) { value ->
        if (value is ProfileAccountUiState.Guest) navigateUp()
    }
}

@Composable
internal fun ProfileDeleteEffect(
    requestAppleAuthorization: () -> Unit,
    snackbarHostState: SnackbarHostState,
    effect: Flow<ProfileDeleteEffect> = emptyFlow(),
) {
    val deleteFailedMessage = stringResource(Res.string.profile_delete_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is ProfileDeleteEffect.RequestAppleAuthorization -> requestAppleAuthorization()
            is ProfileDeleteEffect.DeleteFailed -> snackbarHostState.showImmediate(message = deleteFailedMessage)
        }
    }
}
