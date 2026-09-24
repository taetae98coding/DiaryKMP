package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import io.github.taetae98coding.diary.core.fcm.impl.R

// 서버가 보내는 메시지의 channel_id와 같아야 시스템이 이 채널로 표시한다.
internal const val DAILY_MEMO_NOTIFICATION_CHANNEL_ID: String = "dailyMemo"

internal fun Context.createDailyMemoNotificationChannel() {
    val manager = getSystemService(NotificationManager::class.java) ?: return
    val channel =
        NotificationChannel(
            DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
            getString(R.string.daily_memo_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).also { channel -> channel.description = getString(R.string.daily_memo_notification_channel_description) }

    manager.createNotificationChannel(channel)
}
