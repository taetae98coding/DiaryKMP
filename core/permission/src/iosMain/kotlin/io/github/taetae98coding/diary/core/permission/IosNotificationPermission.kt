package io.github.taetae98coding.diary.core.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

internal suspend fun isNotificationPermissionGranted(): Boolean = notificationAuthorizationStatus() in AUTHORIZED_NOTIFICATION_STATUS_LIST

// 권한이 이미 거부로 결정되어 있으면 시스템은 요청을 표시하지 않고 결정된 결과를 그대로 전달한다.
internal suspend fun requestNotificationPermission(): PermissionResult {
    val isGranted =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter
                .currentNotificationCenter()
                .requestAuthorizationWithOptions(UNAuthorizationOptionAlert) { isGranted, _ ->
                    if (continuation.isActive) continuation.resume(isGranted)
                }
        }

    return if (isGranted) PermissionResult.GRANTED else PermissionResult.DENIED
}

private suspend fun notificationAuthorizationStatus(): UNAuthorizationStatus =
    suspendCancellableCoroutine { continuation ->
        UNUserNotificationCenter
            .currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                if (continuation.isActive) continuation.resume(settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined)
            }
    }

private val AUTHORIZED_NOTIFICATION_STATUS_LIST =
    listOf(
        UNAuthorizationStatusAuthorized,
        UNAuthorizationStatusProvisional,
        UNAuthorizationStatusEphemeral,
    )
