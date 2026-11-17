package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import io.github.taetae98coding.diary.core.fcm.impl.R

// 서버가 보내는 메시지의 channel_id와 같아야 시스템이 이 채널로 표시한다.
internal const val DAILY_NOTIFICATION_CHANNEL_ID: String = "dailyNotification"

internal fun Context.createDailyNotificationChannel() {
    val manager = getSystemService(NotificationManager::class.java) ?: return
    val channel =
        NotificationChannel(
            DAILY_NOTIFICATION_CHANNEL_ID,
            getString(R.string.daily_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).also { channel ->
            channel.description = getString(R.string.daily_notification_channel_description)
            // 이미 만들어진 채널의 설정은 앱이 바꿀 수 없어, 이 설정은 채널을 처음 만드는 설치에만 적용된다.
            channel.setShowBadge(false)
        }

    manager.createNotificationChannel(channel)
}
