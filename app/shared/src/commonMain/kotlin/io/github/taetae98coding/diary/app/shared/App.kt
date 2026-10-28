package io.github.taetae98coding.diary.app.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.analytics.ScreenViewEffect
import io.github.taetae98coding.diary.app.shared.fcm.SubmitFcmTokenEffect
import io.github.taetae98coding.diary.app.shared.integrity.AppPlayIntegrityViewModel
import io.github.taetae98coding.diary.app.shared.integrity.PlayIntegrityLogEffect
import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink
import io.github.taetae98coding.diary.app.shared.navigation.OpenDeepLinkEffect
import io.github.taetae98coding.diary.app.shared.scaffold.AppScaffold
import io.github.taetae98coding.diary.compose.core.image.DiaryImageLoaderEffect
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.RequestPermissionEffect
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun App(modifier: Modifier = Modifier) {
    val syncViewModel = koinViewModel<AppSyncViewModel>()
    val periodicSyncViewModel = koinViewModel<AppPeriodicSyncViewModel>()
    val fcmTokenViewModel = koinViewModel<AppFcmTokenViewModel>()
    val chromeSessionViewModel = koinViewModel<AppChromeSessionViewModel>()
    val playIntegrityViewModel = koinViewModel<AppPlayIntegrityViewModel>()
    val fileUploadViewModel = koinViewModel<AppFileUploadViewModel>()
    val appState = rememberAppState()
    val snackbarHostState = remember { SnackbarHostState() }

    DiaryImageLoaderEffect()
    RequestPermissionEffect(
        permission = Permission.NOTIFICATION,
        onResult = {},
    )
    SyncEffect(
        requestSync = syncViewModel::requestSync,
        uiState = syncViewModel.uiState,
    )
    SchedulePeriodicSyncEffect(
        schedulePeriodicSync = periodicSyncViewModel::schedulePeriodicSync,
        uiState = periodicSyncViewModel.uiState,
    )
    SubmitFcmTokenEffect(
        submit = fcmTokenViewModel::submit,
        uiState = fcmTokenViewModel.uiState,
    )
    ReconcileFileUploadEffect(
        reconcile = fileUploadViewModel::reconcile,
        uiState = fileUploadViewModel.uiState,
    )
    ChromeSessionImportEffect(requestImport = chromeSessionViewModel::requestImport)
    DiaryWebSessionEffect(session = chromeSessionViewModel.session)
    ChromeSessionImportFailedEffect(
        snackbarHostState = snackbarHostState,
        effect = chromeSessionViewModel.effect,
    )
    PlayIntegrityLogEffect(log = playIntegrityViewModel::log)
    ScreenViewEffect(
        log = DiaryLogger::log,
        appState = appState,
    )
    OpenDeepLinkEffect(
        deepLink = AppDeepLink.deepLink,
        appState = appState,
    )

    AppContent(
        appState = appState,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun AppContent(
    appState: AppState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    DiaryTheme {
        Box(modifier = modifier) {
            AppScaffold(
                appState = appState,
                modifier = Modifier.fillMaxSize(),
            )
            // 어느 화면을 보고 있든 앱 전체의 알림을 보여 주므로 화면별 Scaffold가 아니라 앱 루트에 둔다.
            SnackbarHost(
                hostState = snackbarHostState,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .safeDrawingPadding(),
            )
        }
    }
}
