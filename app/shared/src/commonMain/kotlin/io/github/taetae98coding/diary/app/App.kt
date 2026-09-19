package io.github.taetae98coding.diary.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.analytics.ScreenViewEffect
import io.github.taetae98coding.diary.app.notification.ScheduleDailyMemoNotificationEffect
import io.github.taetae98coding.diary.app.scaffold.AppScaffold
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

    RequestPermissionEffect(
        permission = Permission.NOTIFICATION,
        onResult = {},
    )
    ScheduleDailyMemoNotificationEffect(schedule = notificationViewModel::schedule)
    SyncEffect(
        account = syncViewModel.account,
        requestSync = syncViewModel::requestSync,
        schedulePeriodicSync = syncViewModel::schedulePeriodicSync,
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
