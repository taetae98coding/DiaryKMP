package io.github.taetae98coding.diary.app.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
internal fun ScheduleDailyMemoNotificationEffect(schedule: () -> Unit) {
    LaunchedEffect(Unit) { schedule() }
}
