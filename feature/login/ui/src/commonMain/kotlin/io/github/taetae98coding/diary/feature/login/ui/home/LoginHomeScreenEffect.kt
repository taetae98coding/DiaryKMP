package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.login.ui.Res
import io.github.taetae98coding.diary.feature.login.ui.login_sign_in_failed_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginHomeScreenEffect(
    navigateUp: () -> Unit,
    snackbarHostState: SnackbarHostState,
    effect: Flow<LoginHomeEffect> = emptyFlow(),
) {
    val signInFailedMessage = stringResource(Res.string.login_sign_in_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is LoginHomeEffect.SignInSucceeded -> {
                navigateUp()
            }

            is LoginHomeEffect.SignInFailed -> {
                snackbarHostState.showImmediate(message = signInFailedMessage)
            }
        }
    }
}
