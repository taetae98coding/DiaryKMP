package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.NotificationManager
import android.content.Context
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyMemoNotificationChannelTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-031 일일 메모 알림 채널은 소리와 화면 위 떠오름이 없는 낮은 중요도로 디자인 문구를 갖고 만들어진다`() {
        context.createDailyMemoNotificationChannel()

        val channel = notificationManager().getNotificationChannel(DAILY_MEMO_NOTIFICATION_CHANNEL_ID)

        channel.importance shouldBe NotificationManager.IMPORTANCE_LOW
        channel.name shouldBe "Daily memo reminder"
        channel.description shouldBe "Reminds you every morning to check today's memos."
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-031 앱 시작 초기화가 채널을 만들고 다시 실행해도 채널은 하나만 남는다`() {
        val initializer = FcmNotificationChannelInitializer()

        initializer.create(context)
        initializer.create(context)

        notificationManager().notificationChannels shouldHaveSize 1
        initializer.dependencies().shouldBeEmpty()
    }

    private fun notificationManager(): NotificationManager = context.getSystemService(NotificationManager::class.java)
}
