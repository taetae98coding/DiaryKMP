package io.github.taetae98coding.diary.notification

public fun interface Notifier {
    public suspend fun notify(notification: Notification)
}
