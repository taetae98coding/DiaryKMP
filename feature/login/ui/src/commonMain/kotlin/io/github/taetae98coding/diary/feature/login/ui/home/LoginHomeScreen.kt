package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.credential.PlatformSignInState
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsException
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManager
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsException
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManager
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsUserCancelException
import io.github.taetae98coding.diary.compose.credential.rememberPlatformSignInState
import io.github.taetae98coding.diary.feature.login.ui.Res
import io.github.taetae98coding.diary.feature.login.ui.login_sign_in_failed_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginHomeScreen(
    navigateUp: () -> Unit,
    googleCredentialsManager: GoogleCredentialsManager,
    appleCredentialsManager: AppleCredentialsManager,
    viewModel: LoginHomeViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val platformSignInState = rememberPlatformSignInState()
    val snackbarHostState = remember { SnackbarHostState() }
    val signInFailedMessage = stringResource(Res.string.login_sign_in_failed_message)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginHomeScreenEffect(
        effect = viewModel.effect,
        navigateUp = navigateUp,
        snackbarHostState = snackbarHostState,
    )

    LoginHomeScaffold(
        onEvent = { event ->
            when (event) {
                is LoginHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is LoginHomeScaffoldEvent.ClickGoogleSignIn -> {
                    coroutineScope.launch {
                        requestGoogleSignIn(
                            platformSignInState = platformSignInState,
                            viewModel = viewModel,
                            credentialsManager = googleCredentialsManager,
                            snackbarHostState = snackbarHostState,
                            signInFailedMessage = signInFailedMessage,
                        )
                    }
                }

                is LoginHomeScaffoldEvent.ClickAppleSignIn -> {
                    coroutineScope.launch {
                        requestAppleSignIn(
                            platformSignInState = platformSignInState,
                            viewModel = viewModel,
                            credentialsManager = appleCredentialsManager,
                            snackbarHostState = snackbarHostState,
                            signInFailedMessage = signInFailedMessage,
                        )
                    }
                }
            }
        },
        modifier = modifier,
        uiStateProvider = { uiState },
        snackbarHostState = snackbarHostState,
    )
}

private suspend fun requestGoogleSignIn(
    platformSignInState: PlatformSignInState,
    viewModel: LoginHomeViewModel,
    credentialsManager: GoogleCredentialsManager,
    snackbarHostState: SnackbarHostState,
    signInFailedMessage: String,
) {
    if (viewModel.uiState.value.isInProgress) return

    try {
        val credential = platformSignInState.signIn { credentialsManager.signIn() }
        viewModel.signInWithGoogle(credential = credential)
    } catch (_: GoogleCredentialsUserCancelException) {
    } catch (_: GoogleCredentialsException) {
        snackbarHostState.showImmediate(message = signInFailedMessage)
    }
}

private suspend fun requestAppleSignIn(
    platformSignInState: PlatformSignInState,
    viewModel: LoginHomeViewModel,
    credentialsManager: AppleCredentialsManager,
    snackbarHostState: SnackbarHostState,
    signInFailedMessage: String,
) {
    if (viewModel.uiState.value.isInProgress) return

    try {
        val credential = platformSignInState.signIn { credentialsManager.signIn().credential }
        viewModel.signInWithApple(credential = credential)
    } catch (_: AppleCredentialsUserCancelException) {
    } catch (_: AppleCredentialsException) {
        snackbarHostState.showImmediate(message = signInFailedMessage)
    }
}
