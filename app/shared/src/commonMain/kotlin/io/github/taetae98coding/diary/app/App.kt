package io.github.taetae98coding.diary.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.notification.ScheduleDailyMemoNotificationEffect
import io.github.taetae98coding.diary.app.scaffold.AppScaffold
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.RequestPermissionEffect
import io.github.taetae98coding.diary.core.permission.Permission
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun App(modifier: Modifier = Modifier) {
    val syncViewModel = koinViewModel<AppSyncViewModel>()
    val notificationViewModel = koinViewModel<AppDailyMemoNotificationViewModel>()

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

    DiaryTheme {
        AppScaffold(
            appState = rememberAppState(),
            modifier = modifier,
        )
    }
}
