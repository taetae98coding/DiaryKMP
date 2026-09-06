package io.github.taetae98coding.diary.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.notification.ScheduleDailyMemoNotificationEffect
import io.github.taetae98coding.diary.app.permission.RequestNotificationPermissionEffect
import io.github.taetae98coding.diary.app.scaffold.AppScaffold
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
public fun App(modifier: Modifier = Modifier) {
    val syncViewModel = koinViewModel<AppSyncViewModel>()
    val notificationViewModel = koinViewModel<AppDailyMemoNotificationViewModel>()

    RequestNotificationPermissionEffect()
    ScheduleDailyMemoNotificationEffect(schedule = notificationViewModel::schedule)
    SyncEffect(
        account = syncViewModel.account,
        requestSync = syncViewModel::requestSync,
    )

    DiaryTheme {
        AppScaffold(
            appState = rememberAppState(),
            modifier = modifier,
        )
    }
}
