package io.github.taetae98coding.diary.core.notification.impl

import io.github.taetae98coding.diary.core.notification.api.DailyMemoNotificationScheduler
import kotlinx.datetime.LocalTime
import platform.Foundation.NSDateComponents
import platform.Foundation.NSString
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.localizedUserNotificationStringForKey

internal class IosDailyMemoNotificationScheduler : DailyMemoNotificationScheduler {
    // 같은 식별자로 다시 등록하면 시스템이 기존 예약을 대신하므로 예약은 언제나 하나로 남는다.
    override suspend fun schedule(time: LocalTime) {
        val content =
            UNMutableNotificationContent().apply {
                // 예약 시점이 아니라 알림이 전달되는 시점에 문구를 고르므로, 예약 뒤에 기기 언어를 바꿔도 알림이 그 언어로 온다.
                setTitle(NSString.localizedUserNotificationStringForKey(DAILY_MEMO_NOTIFICATION_TITLE_KEY, null))
            }

        val dateComponents =
            NSDateComponents().apply {
                hour = time.hour.toLong()
                minute = time.minute.toLong()
            }

        val request =
            UNNotificationRequest.requestWithIdentifier(
                DAILY_MEMO_NOTIFICATION_IDENTIFIER,
                content,
                UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents, true),
            )

        UNUserNotificationCenter
            .currentNotificationCenter()
            .addNotificationRequest(request, null)
    }

    companion object {
        private const val DAILY_MEMO_NOTIFICATION_IDENTIFIER = "daily-memo"
        private const val DAILY_MEMO_NOTIFICATION_TITLE_KEY = "daily_memo_notification_title"
    }
}
