package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FcmMessagingServiceTest {
    private lateinit var context: Context
    private lateinit var service: FcmMessagingService

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        service = Robolectric.setupService(FcmMessagingService::class.java)
    }

    @Test
    fun `알림 메시지를 받으면 서버가 보낸 제목과 본문을 그대로 표시한다`() {
        service.onMessageReceived(
            remoteMessage(
                title = "오늘 확인할 메모가 2개 있어요",
                body = "- 치과 예약\n- 우유 사기",
                channelId = "custom",
                tag = "daily-memo",
            ),
        )

        val posted = shadowOf(notificationManager()).allNotifications.single()

        posted.extras.getString(Notification.EXTRA_TITLE) shouldBe "오늘 확인할 메모가 2개 있어요"
        posted.extras.getString(Notification.EXTRA_TEXT) shouldBe "- 치과 예약\n- 우유 사기"
        posted.channelId shouldBe "custom"
        shadowOf(notificationManager()).activeNotifications.single().tag shouldBe "daily-memo"
    }

    @Test
    fun `메시지에 채널이 없으면 일일 메모 알림 채널로 표시한다`() {
        service.onMessageReceived(remoteMessage(title = "Check today's memos", body = null, channelId = null, tag = null))

        shadowOf(notificationManager()).allNotifications.single().channelId shouldBe DAILY_MEMO_NOTIFICATION_CHANNEL_ID
    }

    @Test
    fun `표시할 알림이 없는 데이터 메시지는 무시한다`() {
        service.onMessageReceived(RemoteMessage(Bundle()))

        shadowOf(notificationManager()).allNotifications.shouldBeEmpty()
    }

    // FCM SDK는 알림 메시지를 gcm.n. 접두어의 번들 키로 전달하고, gcm.n.e가 1일 때만 알림으로 해석한다.
    private fun remoteMessage(
        title: String,
        body: String?,
        channelId: String?,
        tag: String?,
    ): RemoteMessage =
        RemoteMessage(
            Bundle().apply {
                putString("gcm.n.e", "1")
                putString("gcm.n.title", title)
                body?.let { putString("gcm.n.body", it) }
                channelId?.let { putString("gcm.n.android_channel_id", it) }
                tag?.let { putString("gcm.n.tag", it) }
            },
        )

    private fun notificationManager(): NotificationManager = context.getSystemService(NotificationManager::class.java)
}
