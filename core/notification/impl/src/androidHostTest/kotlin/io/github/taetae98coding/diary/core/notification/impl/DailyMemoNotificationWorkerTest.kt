package io.github.taetae98coding.diary.core.notification.impl

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyMemoNotificationWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        addLauncherActivity()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-001 알림 작업이 실행되면 오늘의 메모를 확인하라는 알림이 하나 발생한다`() {
        doWork() shouldBe ListenableWorker.Result.success()

        val notificationList = shadowOf(notificationManager()).allNotifications

        notificationList shouldHaveSize 1
        notificationList.single().extras.getString(Notification.EXTRA_TITLE) shouldBe "오늘의 메모를 확인하세요"
    }

    @Test
    @Config(qualifiers = "en")
    fun `기기 언어가 한국어가 아니면 알림 문구가 그 언어로 발생한다`() {
        doWork() shouldBe ListenableWorker.Result.success()

        shadowOf(notificationManager())
            .allNotifications
            .single()
            .extras
            .getString(Notification.EXTRA_TITLE) shouldBe "Check today's memos"
    }

    @Test
    fun `알림에는 본문 문구를 두지 않는다`() {
        doWork()

        shadowOf(notificationManager())
            .allNotifications
            .single()
            .extras
            .getString(Notification.EXTRA_TEXT) shouldBe null
    }

    @Test
    fun `알림을 선택하면 앱을 열도록 알림에 실행 대상을 담는다`() {
        doWork()

        shadowOf(notificationManager())
            .allNotifications
            .single()
            .contentIntent
            .shouldNotBeNull()
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-004 같은 알림 작업이 여러 번 실행되어도 알림은 하나만 남는다`() {
        doWork()
        doWork()

        shadowOf(notificationManager()).allNotifications shouldHaveSize 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `알림은 소리와 화면 위 떠오름이 없는 전용 채널로 발생한다`() {
        doWork()

        val notification = shadowOf(notificationManager()).allNotifications.single()
        val channel = notificationManager().getNotificationChannel(notification.channelId)

        notification.channelId shouldBe DAILY_MEMO_NOTIFICATION_CHANNEL_ID
        channel.importance shouldBe NotificationManager.IMPORTANCE_LOW
        channel.name shouldBe "일일 메모 알림"
        channel.description shouldBe "매일 아침 오늘의 메모를 확인하도록 안내합니다."
    }

    // 라이브러리 모듈의 테스트 매니페스트에는 런처 진입점이 없으므로, 앱이 가진 런처 진입점을 테스트 환경에 만들어 준다.
    private fun addLauncherActivity() {
        val componentName = ComponentName(context, "io.github.taetae98coding.diary.TestLauncherActivity")

        shadowOf(context.packageManager).addActivityIfNotPresent(componentName)
        shadowOf(context.packageManager).addIntentFilterForActivity(
            componentName,
            IntentFilter(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) },
        )
    }

    private fun doWork(): ListenableWorker.Result {
        val worker = TestListenableWorkerBuilder<DailyMemoNotificationWorker>(context).build()

        return runBlocking { worker.doWork() }
    }

    private fun notificationManager(): NotificationManager = context.getSystemService(NotificationManager::class.java)
}
