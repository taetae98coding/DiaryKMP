package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.analytics.ScreenViewEffect
import io.github.taetae98coding.diary.app.shared.notification.ScheduleDailyMemoNotificationEffect
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
    val notificationViewModel = koinViewModel<AppDailyMemoNotificationViewModel>()
    val appState = rememberAppState()

    DiaryImageLoaderEffect()
    RequestPermissionEffect(
        permission = Permission.NOTIFICATION,
        onResult = {},
    )
    ScheduleDailyMemoNotificationEffect(
        schedule = notificationViewModel::schedule,
        submitUpcoming = notificationViewModel::submitUpcoming,
        upcoming = notificationViewModel.upcoming,
    )
    SyncEffect(
        requestSync = syncViewModel::requestSync,
        schedulePeriodicSync = syncViewModel::schedulePeriodicSync,
        account = syncViewModel.account,
    )
    ScreenViewEffect(
        log = DiaryLogger::log,
        appState = appState,
    )

    DiaryTheme {
        AppScaffold(
            appState = appState,
            modifier = modifier,
        )
    }
}
