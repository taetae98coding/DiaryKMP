package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.app.shared.analytics.ScreenViewEffect
import io.github.taetae98coding.diary.app.shared.fcm.SubmitFcmTokenEffect
import io.github.taetae98coding.diary.app.shared.integrity.AppPlayIntegrityViewModel
import io.github.taetae98coding.diary.app.shared.integrity.PlayIntegrityLogEffect
import io.github.taetae98coding.diary.app.shared.scaffold.AppScaffold
import io.github.taetae98coding.diary.compose.core.image.DiaryImageLoaderEffect
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.RequestPermissionEffect
import io.github.taetae98coding.diary.compose.web.LocalDiaryWebSession
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun App(modifier: Modifier = Modifier) {
    val syncViewModel = koinViewModel<AppSyncViewModel>()
    val fcmTokenViewModel = koinViewModel<AppFcmTokenViewModel>()
    val chromeSessionViewModel = koinViewModel<AppChromeSessionViewModel>()
    val playIntegrityViewModel = koinViewModel<AppPlayIntegrityViewModel>()
    val appState = rememberAppState()
    val webSession by chromeSessionViewModel.session.collectAsStateWithLifecycle()

    DiaryImageLoaderEffect()
    RequestPermissionEffect(
        permission = Permission.NOTIFICATION,
        onResult = {},
    )
    SyncEffect(
        requestSync = syncViewModel::requestSync,
        schedulePeriodicSync = syncViewModel::schedulePeriodicSync,
        account = syncViewModel.account,
    )
    SubmitFcmTokenEffect(
        submit = fcmTokenViewModel::submit,
        account = fcmTokenViewModel.account,
    )
    ChromeSessionImportEffect(requestImport = chromeSessionViewModel::requestImport)
    PlayIntegrityLogEffect(log = playIntegrityViewModel::log)
    ScreenViewEffect(
        log = DiaryLogger::log,
        appState = appState,
    )

    CompositionLocalProvider(LocalDiaryWebSession provides webSession) {
        DiaryTheme {
            AppScaffold(
                appState = appState,
                modifier = modifier,
            )
        }
    }
}
