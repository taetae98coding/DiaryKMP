package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

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
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-008 알림 메시지를 받으면 서버가 보낸 제목과 본문을 그대로 표시한다`() {
        // 빈 문자열이면 본문이 없는 알림이 되어 다른 경로를 타므로 접두사로 비어 있지 않음을 보장한다.
        val title = "title${fixtureMonkey.giveMeOne<String>()}"
        val body = "- ${fixtureMonkey.giveMeOne<String>()}\n- ${fixtureMonkey.giveMeOne<String>()}"
        val channelId = "channel${fixtureMonkey.giveMeOne<Int>()}"
        val tag = "tag${fixtureMonkey.giveMeOne<Int>()}"

        service.onMessageReceived(remoteMessage(title = title, body = body, channelId = channelId, tag = tag))

        val posted = shadowOf(notificationManager()).allNotifications.single()

        posted.extras.getString(Notification.EXTRA_TITLE) shouldBe title
        posted.extras.getString(Notification.EXTRA_TEXT) shouldBe body
        posted.channelId shouldBe channelId
        shadowOf(notificationManager()).activeNotifications.single().tag shouldBe tag
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-009 메시지에 채널이 없으면 일일 메모 알림 채널로 표시한다`() {
        service.onMessageReceived(remoteMessage(title = "title${fixtureMonkey.giveMeOne<String>()}", body = null, channelId = null, tag = null))

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
