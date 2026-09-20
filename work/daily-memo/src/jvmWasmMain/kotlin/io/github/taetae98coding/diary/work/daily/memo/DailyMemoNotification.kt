package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.notification.Notification

internal expect fun dailyMemoNotification(content: DailyMemoNotificationContent): Notification
