package io.github.taetae98coding.diary.app.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
internal interface NotificationPermissionRequester {
    suspend fun request(): NotificationPermissionRequestResult
}

@Composable
internal expect fun rememberNotificationPermissionRequester(): NotificationPermissionRequester
