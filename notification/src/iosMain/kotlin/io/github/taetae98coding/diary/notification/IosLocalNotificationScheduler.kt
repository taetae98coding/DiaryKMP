package io.github.taetae98coding.diary.notification

import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class IosLocalNotificationScheduler : LocalNotificationScheduler {
    override suspend fun submit(
        identifierPrefix: String,
        requestList: List<LocalNotificationRequest>,
    ) {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        // 이전에 제출한 날짜가 이번 목록에 없으면 대기 예약이 남아 그대로 발생하므로, 같은 접두어의 대기 예약을 먼저 지운다.
        val staleIdentifierList = center.pendingIdentifierList().filter { identifier -> identifier.startsWith(identifierPrefix) }
        center.removePendingNotificationRequestsWithIdentifiers(staleIdentifierList)

        requestList.forEach { request -> center.addNotificationRequest(request.toNotificationRequest(), null) }
    }
}

private suspend fun UNUserNotificationCenter.pendingIdentifierList(): List<String> =
    suspendCoroutine { continuation ->
        getPendingNotificationRequestsWithCompletionHandler { requestList ->
            continuation.resume(requestList.orEmpty().mapNotNull { request -> (request as? UNNotificationRequest)?.identifier })
        }
    }

private fun LocalNotificationRequest.toNotificationRequest(): UNNotificationRequest {
    val content =
        UNMutableNotificationContent().apply {
            setTitle(notification.title)
            if (notification.body.isNotEmpty()) setBody(notification.body)
        }
    val dateComponents =
        NSDateComponents().apply {
            year = dateTime.year.toLong()
            month = dateTime.month.number.toLong()
            day = dateTime.day.toLong()
            hour = dateTime.hour.toLong()
            minute = dateTime.minute.toLong()
        }

    return UNNotificationRequest.requestWithIdentifier(
        notification.id,
        content,
        UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents, false),
    )
}
