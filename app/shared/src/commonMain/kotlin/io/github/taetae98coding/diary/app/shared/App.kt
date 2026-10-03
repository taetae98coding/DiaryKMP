package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
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
    PlayIntegrityLogEffect(log = playIntegrityViewModel::log)
    ScreenViewEffect(
        log = DiaryLogger::log,
        appState = appState,
    )
    OpenDeepLinkEffect(
        deepLink = AppDeepLink.deepLink,
        appState = appState,
    )

    DiaryTheme {
        AppScaffold(
            appState = appState,
            modifier = modifier,
        )
    }
}
