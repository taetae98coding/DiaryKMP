package io.github.taetae98coding.diary.app.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

@Composable
internal actual fun rememberNotificationPermissionRequester(): NotificationPermissionRequester = remember { IosNotificationPermissionRequester() }

private class IosNotificationPermissionRequester : NotificationPermissionRequester {
    // 권한이 이미 결정되어 있으면 시스템은 요청을 표시하지 않고 결정된 결과를 그대로 전달한다.
    override suspend fun request(): NotificationPermissionRequestResult =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter
                .currentNotificationCenter()
                .requestAuthorizationWithOptions(UNAuthorizationOptionAlert) { isGranted, _ ->
                    if (continuation.isActive) {
                        val result =
                            if (isGranted) {
                                NotificationPermissionRequestResult.GRANTED
                            } else {
                                NotificationPermissionRequestResult.DENIED
                            }

                        continuation.resume(result)
                    }
                }
        }
}
