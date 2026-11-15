package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.credential.PlatformSignInState
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsException
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManager
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.compose.credential.rememberPlatformSignInState
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import kotlinx.coroutines.launch

@Composable
internal fun ProfileScreen(
    navigateUp: () -> Unit,
    navigateToProfileImageEdit: () -> Unit,
    appleCredentialsManager: AppleCredentialsManager,
    accountViewModel: ProfileAccountViewModel,
    deleteViewModel: ProfileDeleteViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val platformSignInState = rememberPlatformSignInState()
    val authorizationAttempt = remember { ProfileAppleAuthorizationAttempt() }
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteUiState by deleteViewModel.uiState.collectAsStateWithLifecycle()

    ProfileGuestEffect(navigateUp = navigateUp, accountUiState = accountViewModel.uiState)
    ProfileDeleteEffect(
        requestAppleAuthorization = {
            // 요청을 받기 전에 다이얼로그가 닫혔으면 받지 않는다.
            if (!deleteViewModel.uiState.value.isConfirmVisible) return@ProfileDeleteEffect

            coroutineScope.launch {
                requestAppleAuthorization(
                    platformSignInState = platformSignInState,
                    attempt = authorizationAttempt,
                    viewModel = deleteViewModel,
                    credentialsManager = appleCredentialsManager,
                )
            }
        },
        snackbarHostState = snackbarHostState,
        effect = deleteViewModel.effect,
    )

    ProfileScaffold(
        onEvent = { event ->
            when (event) {
                is ProfileScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is ProfileScaffoldEvent.ClickChangeProfileImage -> {
                    navigateToProfileImageEdit()
                }

                is ProfileScaffoldEvent.ClickDeleteAccount -> {
                    deleteViewModel.showConfirm()
                }

                is ProfileScaffoldEvent.CancelDeleteAccount -> {
                    if (!deleteViewModel.uiState.value.isInProgress) {
                        deleteViewModel.dismissConfirm()
                        platformSignInState.cancel()
                    }
                }

                is ProfileScaffoldEvent.ConfirmDeleteAccount -> {
                    if (!deleteViewModel.uiState.value.isInProgress) {
                        authorizationAttempt.end()
                        platformSignInState.cancel()
                        deleteViewModel.confirmDelete()
                    }
                }
            }
        },
        modifier = modifier,
        deleteUiStateProvider = { deleteUiState },
        isDeleteInProgressProvider = { deleteUiState.isInProgress },
        snackbarHostState = snackbarHostState,
    )
}

// 새 흐름을 열거나 탈퇴를 다시 고르면 이전 흐름이 취소되며 끝나므로, 가장 최근에 시작해 아직 끝내지 않은 흐름만 결과를 반영한다.
private class ProfileAppleAuthorizationAttempt {
    private var latest = 0

    fun start(): Int = ++latest

    fun end() {
        latest += 1
    }

    fun isLatest(attempt: Int): Boolean = attempt == latest
}

private suspend fun requestAppleAuthorization(
    platformSignInState: PlatformSignInState,
    attempt: ProfileAppleAuthorizationAttempt,
    viewModel: ProfileDeleteViewModel,
    credentialsManager: AppleCredentialsManager,
) {
    val current = attempt.start()
    var appleAuthorization: AppleAuthorization? = null
    try {
        appleAuthorization = platformSignInState.signIn { credentialsManager.signIn() }.authorization
        if (appleAuthorization == null && attempt.isLatest(current)) viewModel.failAppleAuthorizationWithoutCode()
    } catch (_: AppleCredentialsUserCancelException) {
    } catch (exception: AppleCredentialsException) {
        viewModel.failAppleAuthorization(throwable = exception)
    } finally {
        if (appleAuthorization == null && attempt.isLatest(current)) viewModel.endAppleAuthorization()
    }

    if (!attempt.isLatest(current)) return

    appleAuthorization?.let { authorization -> viewModel.delete(appleAuthorization = authorization) }
}
