package io.github.taetae98coding.diary.core.fcm.impl.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.fcm.impl.R
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RemoteNotificationPresenterTest {
    private lateinit var context: Context
    private lateinit var presenter: RemoteNotificationPresenter

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        presenter = RemoteNotificationPresenter(context = context)
        addLauncherActivity()
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-009 알림을 표시하면 그 제목과 채널, 앱 알림 아이콘의 알림이 하나 발생한다`() {
        val notification = fixtureMonkey.giveMeOne<RemoteNotification>()

        presenter.present(notification)

        val posted = shadowOf(notificationManager()).allNotifications.single()

        posted.extras.getString(Notification.EXTRA_TITLE) shouldBe notification.title
        posted.channelId shouldBe notification.channelId
        posted.smallIcon.resId shouldBe R.drawable.ic_notification
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-008 본문이 비어 있으면 알림에 본문 문구를 두지 않는다`() {
        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>().copy(body = ""))

        val extras = shadowOf(notificationManager()).allNotifications.single().extras

        extras.getString(Notification.EXTRA_TEXT) shouldBe null
        extras.getString(Notification.EXTRA_BIG_TEXT) shouldBe null
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-008 본문이 있으면 알림 본문에 그대로 담고 펼치면 전체가 보이는 긴 글 스타일을 쓴다`() {
        val body = "- first\n- second\n- third"

        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>().copy(body = body))

        val extras = shadowOf(notificationManager()).allNotifications.single().extras

        extras.getString(Notification.EXTRA_TEXT) shouldBe body
        extras.getString(Notification.EXTRA_BIG_TEXT) shouldBe body
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-003 알림을 선택하면 앱의 시작 지점을 열고 선택한 알림은 사라진다`() {
        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>())

        val posted = shadowOf(notificationManager()).allNotifications.single()
        val launchIntent = shadowOf(posted.contentIntent.shouldNotBeNull()).savedIntent

        launchIntent.component?.className shouldBe LAUNCHER_ACTIVITY_CLASS_NAME
        (posted.flags and Notification.FLAG_AUTO_CANCEL) shouldBe Notification.FLAG_AUTO_CANCEL
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-004 같은 tag의 알림을 여러 번 표시해도 나중 알림 하나만 남는다`() {
        val notification = fixtureMonkey.giveMeOne<RemoteNotification>()
        val laterTitle = "later ${notification.title}"

        presenter.present(notification)
        presenter.present(notification.copy(title = laterTitle))

        val notificationList = shadowOf(notificationManager()).allNotifications

        notificationList shouldHaveSize 1
        notificationList.single().extras.getString(Notification.EXTRA_TITLE) shouldBe laterTitle
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-004 tag가 다른 알림은 서로 대신하지 않는다`() {
        val notification = fixtureMonkey.giveMeOne<RemoteNotification>()

        presenter.present(notification)
        presenter.present(notification.copy(tag = "other ${notification.tag}"))

        shadowOf(notificationManager()).allNotifications shouldHaveSize 2
    }

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
        const val LAUNCHER_ACTIVITY_CLASS_NAME: String = "io.github.taetae98coding.diary.TestLauncherActivity"
    }
}
