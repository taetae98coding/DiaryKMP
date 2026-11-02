package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.credential.AppleCredentialsException
import io.github.taetae98coding.diary.compose.credential.AppleCredentialsManager
import io.github.taetae98coding.diary.compose.credential.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.compose.credential.PlatformSignInState
import io.github.taetae98coding.diary.compose.credential.authorize
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
            val uiState = deleteViewModel.uiState.value
            // 다이얼로그가 닫혔거나, 재생성 전에 보낸 요청이라 이전 화면이 사라지며 진행 중을 풀었으면 받지 않는다.
            if (!uiState.isConfirmVisible) return@ProfileDeleteEffect
            if (appleCredentialsManager.isSignInEndDetectable && !uiState.isInProgress) return@ProfileDeleteEffect

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
    CancelAppleAuthorizationOnDisposeEffect(deleteViewModel = deleteViewModel)

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
                    deleteViewModel.confirmDelete(isSignInEndDetectable = appleCredentialsManager.isSignInEndDetectable)
                }
            }
        },
        modifier = modifier,
        deleteUiStateProvider = { deleteUiState },
        isDeleteInProgressProvider = { deleteUiState.isInProgress || platformSignInState.isInProgress },
        snackbarHostState = snackbarHostState,
    )
}

// 흐름의 끝을 알 수 없는 플랫폼에서 새 흐름을 열면 이전 흐름이 취소되므로, 가장 최근에 시작한 흐름만 탈퇴 대기로 되돌린다.
private class ProfileAppleAuthorizationAttempt {
    private var latest = 0

    fun start(): Int = ++latest

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
        appleAuthorization =
            platformSignInState.signIn(isEndDetectable = credentialsManager.isSignInEndDetectable) {
                credentialsManager.authorize()
            }
    } catch (_: AppleCredentialsUserCancelException) {
    } catch (exception: AppleCredentialsException) {
        viewModel.failAppleAuthorization(throwable = exception)
    } finally {
        if (appleAuthorization == null && attempt.isLatest(current)) viewModel.endAppleAuthorization()
    }

    appleAuthorization?.let { authorization -> viewModel.delete(appleAuthorization = authorization) }
}

// 화면이 사라지면 그 화면이 기다리던 Apple 재인증은 끝나지 않으므로, 기다리던 진행 중 상태를 푼다.
@Composable
private fun CancelAppleAuthorizationOnDisposeEffect(deleteViewModel: ProfileDeleteViewModel) {
    DisposableEffect(deleteViewModel) {
        onDispose { deleteViewModel.cancelAppleAuthorization() }
    }
}
