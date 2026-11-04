package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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
        addLauncherActivity()
    }

    @Test
    fun `TC-22429a81-350f-4c39-ae40-5ee3b9a8d5d3 알림 메시지를 받으면 서버가 보낸 제목과 본문을 그대로 표시한다`() {
        // 빈 문자열이면 본문이 없는 알림이 되어 다른 경로를 타므로 접두사로 비어 있지 않음을 보장한다.
        val title = title()
        val body = "- ${Arb.string().next()}\n- ${Arb.string().next()}"
        val channelId = "channel${Arb.int().next()}"
        val tag = "tag${Arb.int().next()}"

        service.onMessageReceived(remoteMessage(title = title, body = body, channelId = channelId, tag = tag))

        val posted = shadowOf(notificationManager()).allNotifications.single()

        posted.extras.getString(Notification.EXTRA_TITLE) shouldBe title
        posted.extras.getString(Notification.EXTRA_TEXT) shouldBe body
        posted.channelId shouldBe channelId
        shadowOf(notificationManager()).activeNotifications.single().tag shouldBe tag
    }

    @Test
    fun `TC-8bd47d8e-d759-4876-a1b2-be83de3bddb7 메시지에 채널이 없으면 일일 메모 알림 채널로 표시한다`() {
        service.onMessageReceived(remoteMessage(title = title(), body = null, channelId = null, tag = null))

        shadowOf(notificationManager()).allNotifications.single().channelId shouldBe DAILY_MEMO_NOTIFICATION_CHANNEL_ID
    }

    @Test
    fun `TC-18f273e4-70a3-43f2-a3ee-c64bcc64f8ef 메시지에 담긴 열 화면을 표시한 알림이 그대로 담는다`() {
        listOf(
            TIMETABLE_DEEP_LINK to TIMETABLE_DEEP_LINK,
            null to null,
        ).forEach { (deepLink, expected) ->
            notificationManager().cancelAll()

            service.onMessageReceived(
                remoteMessage(title = title(), body = null, channelId = null, tag = null, deepLink = deepLink),
            )

            val posted = shadowOf(notificationManager()).allNotifications.single()

            shadowOf(posted.contentIntent).savedIntent.dataString shouldBe expected
        }
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
        deepLink: String? = null,
    ): RemoteMessage =
        RemoteMessage(
            Bundle().apply {
                // 서버가 보낸 추가 정보는 접두어 없는 키로 전달된다.
                deepLink?.let { putString(ScreenDeepLink.NOTIFICATION_DATA_KEY, it) }
                putString("gcm.n.e", "1")
                putString("gcm.n.title", title)
                body?.let { putString("gcm.n.body", it) }
                channelId?.let { putString("gcm.n.android_channel_id", it) }
                tag?.let { putString("gcm.n.tag", it) }
            },
        )

    private fun title(): String = "title${Arb.string().next()}"

    // 라이브러리 모듈의 테스트 매니페스트에는 런처 진입점이 없으므로, 앱이 가진 런처 진입점을 테스트 환경에 만들어 준다.
    private fun addLauncherActivity() {
        val componentName = ComponentName(context, LAUNCHER_ACTIVITY_CLASS_NAME)

        shadowOf(context.packageManager).addActivityIfNotPresent(componentName)
        shadowOf(context.packageManager).addIntentFilterForActivity(
            componentName,
            IntentFilter(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) },
        )
    }

    private fun notificationManager(): NotificationManager = context.getSystemService(NotificationManager::class.java)

    private companion object {
        // 서버가 2026년 11월 4일에 발생한 일일 메모 알림에 담아 보내는 주소다.
        private const val TIMETABLE_DEEP_LINK: String = "diary://timetable?type=day&date=2026-11-04"

        const val LAUNCHER_ACTIVITY_CLASS_NAME: String = "io.github.taetae98coding.diary.TestLauncherActivity"
    }
}
