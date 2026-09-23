package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.login.ui.Res
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsUserCancelException
import io.github.taetae98coding.diary.feature.login.ui.credential.rememberAppleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.rememberGoogleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.login_sign_in_failed_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LoginHomeScreen(
    navigateUp: () -> Unit,
    googleCredentialsManager: GoogleCredentialsManager,
    appleCredentialsManager: AppleCredentialsManager,
    viewModel: LoginHomeViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val signInFailedMessage = stringResource(Res.string.login_sign_in_failed_message)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SignInEffect(
        effect = viewModel.effect,
        navigateUp = navigateUp,
        hostState = hostState,
    )

    LoginHomeScaffold(
        uiStateProvider = { uiState },
        onEvent = { event ->
            when (event) {
                is LoginHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is LoginHomeScaffoldEvent.ClickGoogleSignIn -> {
                    coroutineScope.launch {
                        requestGoogleSignIn(
                            viewModel = viewModel,
                            credentialsManager = googleCredentialsManager,
                            hostState = hostState,
                            signInFailedMessage = signInFailedMessage,
                        )
                    }
                }

                is LoginHomeScaffoldEvent.ClickAppleSignIn -> {
                    coroutineScope.launch {
                        requestAppleSignIn(
                            viewModel = viewModel,
                            credentialsManager = appleCredentialsManager,
                            hostState = hostState,
                            signInFailedMessage = signInFailedMessage,
                        )
                    }
                }
            }
        },
        modifier = modifier,
        hostState = hostState,
    )
}

private suspend fun requestGoogleSignIn(
    viewModel: LoginHomeViewModel,
    credentialsManager: GoogleCredentialsManager,
    hostState: SnackbarHostState,
    signInFailedMessage: String,
) {
    try {
        viewModel.signInWithGoogle(credential = credentialsManager.signIn())
    } catch (_: GoogleCredentialsUserCancelException) {
    } catch (_: GoogleCredentialsException) {
        hostState.showImmediate(message = signInFailedMessage)
    }
}

private suspend fun requestAppleSignIn(
    viewModel: LoginHomeViewModel,
    credentialsManager: AppleCredentialsManager,
    hostState: SnackbarHostState,
    signInFailedMessage: String,
) {
    try {
        viewModel.signInWithApple(credential = credentialsManager.signIn())
    } catch (_: AppleCredentialsUserCancelException) {
    } catch (_: AppleCredentialsException) {
        hostState.showImmediate(message = signInFailedMessage)
    }
}

@Composable
private fun SignInEffect(
    navigateUp: () -> Unit,
    effect: Flow<LoginHomeEffect> = emptyFlow(),
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val signInFailedMessage = stringResource(Res.string.login_sign_in_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is LoginHomeEffect.SignInSucceeded -> {
                navigateUp()
            }

            is LoginHomeEffect.SignInFailed -> {
                hostState.showImmediate(message = signInFailedMessage)
            }
        }
    }
}
