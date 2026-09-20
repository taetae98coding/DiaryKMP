package io.github.taetae98coding.diary.notification

public data class Notification(
    val id: String,
    val channel: NotificationChannel,
    val title: String,
    // 빈 문자열이면 본문을 두지 않는다.
    val body: String,
)
