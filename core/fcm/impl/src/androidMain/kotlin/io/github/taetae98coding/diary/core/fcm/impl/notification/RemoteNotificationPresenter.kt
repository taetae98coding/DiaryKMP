package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.taetae98coding.diary.core.fcm.impl.R

// FCM SDK는 앱이 앞에 있는 동안 받은 알림 메시지를 표시하지 않고 넘겨주므로, 백그라운드에서 SDK가 표시하는 것과 같은 모양으로 직접 표시한다.
internal class RemoteNotificationPresenter(
    private val context: Context,
) {
    fun present(notification: RemoteNotification) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val androidNotification =
            Notification
                .Builder(context, notification.channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notification.title)
                .setBody(notification.body)
                .setContentIntent(context.launchAppPendingIntent(deepLink = notification.deepLink))
                .setAutoCancel(true)
                .build()

        manager.notify(notification.tag, REMOTE_NOTIFICATION_ID, androidNotification)
    }

    private fun Notification.Builder.setBody(body: String): Notification.Builder {
        if (body.isEmpty()) return this

        return setContentText(body).setStyle(Notification.BigTextStyle().bigText(body))
    }

    private companion object {
        // 알림은 tag로 구분하므로 ID는 하나로 고정한다.
        const val REMOTE_NOTIFICATION_ID: Int = 0
    }
}

// 이 모듈이 앱의 화면 구성을 알지 않도록 런처가 쓰는 진입점을 그대로 열고, 주소가 있으면 주소만 실어 보낸다.
// 주소를 실을 때는 떠 있는 진입 화면을 다시 만들지 않고 그 화면이 주소를 받게 해서 보던 화면을 남긴다.
private fun Context.launchAppPendingIntent(deepLink: String): PendingIntent? {
    val intent =
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            if (deepLink.isNotEmpty()) {
                data = Uri.parse(deepLink)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        } ?: return null

    return PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
