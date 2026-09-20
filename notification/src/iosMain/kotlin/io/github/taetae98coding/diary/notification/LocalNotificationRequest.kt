package io.github.taetae98coding.diary.notification

import kotlinx.datetime.LocalDateTime

public data class LocalNotificationRequest(
    val notification: Notification,
    val dateTime: LocalDateTime,
)
