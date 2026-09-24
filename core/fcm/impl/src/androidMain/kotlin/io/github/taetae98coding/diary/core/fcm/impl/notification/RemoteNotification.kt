package io.github.taetae98coding.diary.core.fcm.impl.notification

internal data class RemoteNotification(
    val title: String,
    val body: String,
    val channelId: String,
    // 같은 tag의 알림은 시스템이 앞서 표시한 알림을 대신한다.
    val tag: String,
)
