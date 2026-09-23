package io.github.taetae98coding.diary.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.app.Notification as AndroidNotification
import android.app.NotificationChannel as AndroidNotificationChannel

internal class AndroidNotifier(
    private val context: Context,
) : Notifier {
    override suspend fun notify(notification: Notification) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(notification.channel.toAndroidChannel())

        val androidNotification =
            AndroidNotification
                .Builder(context, notification.channel.id)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notification.title)
                .setBody(notification.body)
                .setContentIntent(context.launchAppPendingIntent())
                .setAutoCancel(true)
                .build()

        // 같은 알림 ID를 쓰면 시스템이 앞서 표시한 알림을 새 알림으로 대신하므로 같은 알림은 하나만 남는다.
        manager.notify(notification.id.hashCode(), androidNotification)
    }

    private fun AndroidNotification.Builder.setBody(body: String): AndroidNotification.Builder {
        if (body.isEmpty()) return this

        return setContentText(body).setStyle(AndroidNotification.BigTextStyle().bigText(body))
    }

    private fun NotificationChannel.toAndroidChannel(): AndroidNotificationChannel =
        AndroidNotificationChannel(
            id,
            name,
            if (isSilent) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_DEFAULT,
        ).also { channel -> channel.description = description }
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
