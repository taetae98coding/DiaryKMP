package io.github.taetae98coding.diary.app.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberNotificationPermissionRequester(): NotificationPermissionRequester = remember { JvmNotificationPermissionRequester() }

internal class JvmNotificationPermissionRequester : NotificationPermissionRequester {
    // JVM 데스크톱은 알림 권한 요청을 제공하지 않으므로 시스템 요청 없이 거부로 처리한다.
    override suspend fun request(): NotificationPermissionRequestResult = NotificationPermissionRequestResult.DENIED
}
