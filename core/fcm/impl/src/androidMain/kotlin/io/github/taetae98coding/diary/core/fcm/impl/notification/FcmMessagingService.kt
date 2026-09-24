package io.github.taetae98coding.diary.core.fcm.impl.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

internal class FcmMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return

        RemoteNotificationPresenter(context = this).present(
            RemoteNotification(
                title = notification.title.orEmpty(),
                body = notification.body.orEmpty(),
                channelId = notification.channelId ?: DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
                tag = notification.tag ?: DEFAULT_TAG,
            ),
        )
    }

    private companion object {
        const val DEFAULT_TAG: String = "fcm"
    }
}
