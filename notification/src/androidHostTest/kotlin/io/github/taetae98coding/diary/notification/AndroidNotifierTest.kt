package io.github.taetae98coding.diary.notification

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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
import android.app.Notification as AndroidNotification

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidNotifierTest {
    private lateinit var context: Context
    private lateinit var notifier: AndroidNotifier

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        notifier = AndroidNotifier(context = context)
        addLauncherActivity()
    }

    @Test
    fun `알림을 보내면 그 제목의 알림이 하나 발생한다`() {
        val notification = fixtureMonkey.giveMeOne<Notification>()

        notify(notification)

        val notificationList = shadowOf(notificationManager()).allNotifications

        notificationList shouldHaveSize 1
        notificationList.single().extras.getString(AndroidNotification.EXTRA_TITLE) shouldBe notification.title
    }

    @Test
    fun `알림에는 본문 문구를 두지 않는다`() {
        notify(fixtureMonkey.giveMeOne<Notification>())

        shadowOf(notificationManager())
            .allNotifications
            .single()
            .extras
            .getString(AndroidNotification.EXTRA_TEXT) shouldBe null
    }

    @Test
    fun `알림을 선택하면 앱을 열도록 알림에 실행 대상을 담는다`() {
        notify(fixtureMonkey.giveMeOne<Notification>())

        shadowOf(notificationManager())
            .allNotifications
            .single()
            .contentIntent
            .shouldNotBeNull()
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-004 같은 ID의 알림을 여러 번 보내도 알림은 하나만 남는다`() {
        val notification = fixtureMonkey.giveMeOne<Notification>()

        notify(notification)
        notify(notification.copy(title = "prefix ${notification.title}"))

        shadowOf(notificationManager()).allNotifications shouldHaveSize 1
    }

    @Test
    fun `채널이 무음이면 소리와 화면 위 떠오름이 없는 낮은 중요도 채널로 발생한다`() {
        val notification = fixtureMonkey.giveMeOne<Notification>().let { it.copy(channel = it.channel.copy(isSilent = true)) }

        notify(notification)

        val posted = shadowOf(notificationManager()).allNotifications.single()
        val channel = notificationManager().getNotificationChannel(posted.channelId)

        posted.channelId shouldBe notification.channel.id
        channel.importance shouldBe NotificationManager.IMPORTANCE_LOW
        channel.name shouldBe notification.channel.name
        channel.description shouldBe notification.channel.description
    }

    @Test
    fun `채널이 무음이 아니면 기본 중요도 채널로 발생한다`() {
        val notification = fixtureMonkey.giveMeOne<Notification>().let { it.copy(channel = it.channel.copy(isSilent = false)) }

        notify(notification)

        val posted = shadowOf(notificationManager()).allNotifications.single()

        notificationManager().getNotificationChannel(posted.channelId).importance shouldBe NotificationManager.IMPORTANCE_DEFAULT
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

    private fun notify(notification: Notification) {
        runBlocking { notifier.notify(notification) }
    }

    private fun notificationManager(): NotificationManager = context.getSystemService(NotificationManager::class.java)
}
