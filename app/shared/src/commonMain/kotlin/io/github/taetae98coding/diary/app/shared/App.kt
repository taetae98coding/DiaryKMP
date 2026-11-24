package io.github.taetae98coding.diary.app.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.app.shared.analytics.ScreenViewEffect
import io.github.taetae98coding.diary.app.shared.fcm.SubmitFcmTokenEffect
import io.github.taetae98coding.diary.app.shared.integrity.AppPlayIntegrityViewModel
import io.github.taetae98coding.diary.app.shared.integrity.PlayIntegrityLogEffect
import io.github.taetae98coding.diary.app.shared.navigation.AppNavigation
import io.github.taetae98coding.diary.app.shared.navigation.OpenDeepLinkEffect
import io.github.taetae98coding.diary.app.shared.scaffold.AppScaffold
import io.github.taetae98coding.diary.app.shared.web.ProvideDiaryWebSession
import io.github.taetae98coding.diary.app.shared.web.RestoreWebLoginEffect
import io.github.taetae98coding.diary.app.shared.web.SaveWebLoginEffect
import io.github.taetae98coding.diary.compose.core.image.DiaryImageLoaderEffect
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.permission.RequestPermissionEffect
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.logger.core.DiaryLogger

@Composable
public fun App(
    dependencies: AppDependencies,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalMetroViewModelFactory provides dependencies.viewModelFactory) {
        AppRoot(
            dependencies = dependencies,
            modifier = modifier,
        )
    }
}

@Composable
private fun AppRoot(
    dependencies: AppDependencies,
    modifier: Modifier = Modifier,
) {
    val syncViewModel = metroViewModel<AppSyncViewModel>()
    val periodicSyncViewModel = metroViewModel<AppPeriodicSyncViewModel>()
    val fcmTokenViewModel = metroViewModel<AppFcmTokenViewModel>()
    val playIntegrityViewModel = metroViewModel<AppPlayIntegrityViewModel>()
    val fileUploadViewModel = metroViewModel<AppFileUploadViewModel>()
    val webLoginViewModel = metroViewModel<AppWebLoginViewModel>()
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
    PlayIntegrityLogEffect(log = playIntegrityViewModel::log)
    RestoreWebLoginEffect(restore = webLoginViewModel::restore)
    SaveWebLoginEffect(save = webLoginViewModel::save)
    ScreenViewEffect(
        log = DiaryLogger::log,
        appState = appState,
    )
    OpenDeepLinkEffect(
        deepLink = dependencies.appDeepLink.deepLink,
        deepLinkNavigationList = dependencies.deepLinkNavigationList,
        navigator = appState,
    )

    CompositionLocalProvider(LocalDiaryMapContentOverride provides dependencies.mapContentOverride) {
        ProvideDiaryWebSession(session = webLoginViewModel.session) {
            AppContent(
                appState = appState,
                entryInstallerList = dependencies.entryInstallerList,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun AppContent(
    appState: AppState,
    entryInstallerList: List<ScreenEntryInstaller>,
    modifier: Modifier = Modifier,
) {
    DiaryTheme {
        AppScaffold(
            appState = appState,
            modifier = modifier,
        ) {
            AppNavigation(
                entryInstallerList = entryInstallerList,
                appState = appState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
