package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.login.ui.Res
import io.github.taetae98coding.diary.feature.login.ui.login_home_title
import io.github.taetae98coding.diary.feature.login.ui.login_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginHomeScaffold(
    onEvent: (LoginHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> LoginHomeUiState = { LoginHomeUiState() },
    platformSignInState: LoginPlatformSignInState = rememberLoginPlatformSignInState(),
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.login_home_title),
                onNavigateUp = { onEvent(LoginHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.login_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = hostState) },
    ) { paddingValues ->
        DiaryCrossfade(
            targetState = uiStateProvider().isInProgress || platformSignInState.isInProgress,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) { isInProgress ->
            if (isInProgress) {
                DiaryLoadingBox(modifier = Modifier.fillMaxSize())
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoginSignInButtonRow(onEvent = onEvent)
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun LoginHomeScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        LoginHomeScaffold(
            onEvent = {},
            uiStateProvider = { LoginHomeUiState(isInProgress = isInProgress) },
        )
    }
}
