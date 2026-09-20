package io.github.taetae98coding.diary.notification

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal class IosNotifier : Notifier {
    // 같은 식별자로 다시 등록하면 시스템이 앞서 표시한 알림을 대신하므로 같은 알림은 하나만 남는다.
    override suspend fun notify(notification: Notification) {
        val content =
            UNMutableNotificationContent().apply {
                setTitle(notification.title)
                if (notification.body.isNotEmpty()) setBody(notification.body)
            }
        val request = UNNotificationRequest.requestWithIdentifier(notification.id, content, null)

        UNUserNotificationCenter
            .currentNotificationCenter()
            .addNotificationRequest(request, null)
    }
}
