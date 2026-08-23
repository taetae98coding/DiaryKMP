package io.github.taetae98coding.diary.core.notification.impl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context

internal const val DAILY_MEMO_NOTIFICATION_CHANNEL_ID: String = "dailyMemo"
internal const val DAILY_MEMO_NOTIFICATION_ID: Int = 1

internal fun Context.notifyDailyMemo() {
    val manager = getSystemService(NotificationManager::class.java) ?: return

    manager.createNotificationChannel(
        NotificationChannel(
            DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
            getString(R.string.daily_memo_notification_channel_name),
            // 소리와 화면 위 떠오름 없이 알림 목록과 상태 표시줄에만 남긴다.
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.daily_memo_notification_channel_description)
        },
    )

    val notification =
        Notification
            .Builder(this, DAILY_MEMO_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_daily_memo_notification)
            .setContentTitle(getString(R.string.daily_memo_notification_title))
            .setContentIntent(launchAppPendingIntent())
            .setAutoCancel(true)
            .build()

    // 같은 알림 ID를 쓰면 시스템이 앞서 표시한 알림을 새 알림으로 대신하므로 하루에 하나만 남는다.
    manager.notify(DAILY_MEMO_NOTIFICATION_ID, notification)
}

// 알림 모듈이 앱의 시작 화면을 알지 않도록 런처가 쓰는 진입점을 그대로 연다.
private fun Context.launchAppPendingIntent(): PendingIntent? {
    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null

    return PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
