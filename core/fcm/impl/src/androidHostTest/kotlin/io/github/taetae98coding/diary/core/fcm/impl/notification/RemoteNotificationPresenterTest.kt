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
    fun `TC-8bd47d8e-d759-4876-a1b2-be83de3bddb7 알림을 표시하면 그 제목과 채널, 앱 알림 아이콘의 알림이 하나 발생한다`() {
        val notification = fixtureMonkey.giveMeOne<RemoteNotification>()

        presenter.present(notification)

        val posted = shadowOf(notificationManager()).allNotifications.single()

        posted.extras.getString(Notification.EXTRA_TITLE) shouldBe notification.title
        posted.channelId shouldBe notification.channelId
        posted.smallIcon.resId shouldBe R.drawable.ic_notification
    }

    @Test
    fun `TC-22429a81-350f-4c39-ae40-5ee3b9a8d5d3 본문이 비어 있으면 알림에 본문 문구를 두지 않는다`() {
        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>().copy(body = ""))

        val extras = shadowOf(notificationManager()).allNotifications.single().extras

        extras.getString(Notification.EXTRA_TEXT) shouldBe null
        extras.getString(Notification.EXTRA_BIG_TEXT) shouldBe null
    }

    @Test
    fun `TC-22429a81-350f-4c39-ae40-5ee3b9a8d5d3 본문이 있으면 알림 본문에 그대로 담고 펼치면 전체가 보이는 긴 글 스타일을 쓴다`() {
        val body = "- first\n- second\n- third"

        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>().copy(body = body))

        val extras = shadowOf(notificationManager()).allNotifications.single().extras

        extras.getString(Notification.EXTRA_TEXT) shouldBe body
        extras.getString(Notification.EXTRA_BIG_TEXT) shouldBe body
    }

    @Test
    fun `TC-3788b5fc-b522-403e-897a-7f8a5b9565ad 알림을 선택하면 알림 날짜의 시간표 주소로 앱을 열고 알림은 사라진다`() {
        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>().copy(deepLink = TIMETABLE_DEEP_LINK))

        val posted = shadowOf(notificationManager()).allNotifications.single()
        val launchIntent = shadowOf(posted.contentIntent.shouldNotBeNull()).savedIntent

        launchIntent.component?.className shouldBe LAUNCHER_ACTIVITY_CLASS_NAME
        launchIntent.dataString shouldBe TIMETABLE_DEEP_LINK
        (posted.flags and Notification.FLAG_AUTO_CANCEL) shouldBe Notification.FLAG_AUTO_CANCEL
    }

    @Test
    fun `열 화면이 있으면 떠 있는 진입 화면이 주소를 받도록 연다`() {
        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>().copy(deepLink = TIMETABLE_DEEP_LINK))

        val launchIntent = shadowOf(shadowOf(notificationManager()).allNotifications.single().contentIntent).savedIntent
        val reuseFlags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        (launchIntent.flags and reuseFlags) shouldBe reuseFlags
    }

    @Test
    fun `TC-18f273e4-70a3-43f2-a3ee-c64bcc64f8ef 열 화면이 없으면 주소 없이 앱만 연다`() {
        presenter.present(fixtureMonkey.giveMeOne<RemoteNotification>().copy(deepLink = ""))

        val launchIntent = shadowOf(shadowOf(notificationManager()).allNotifications.single().contentIntent).savedIntent

        launchIntent.component?.className shouldBe LAUNCHER_ACTIVITY_CLASS_NAME
        launchIntent.data shouldBe null
    }

    @Test
    fun `TC-2a9d4211-fb15-455a-a888-72323919b7b9 같은 tag의 알림을 여러 번 표시해도 나중 알림 하나만 남는다`() {
        val notification = fixtureMonkey.giveMeOne<RemoteNotification>()
        val laterTitle = "later ${notification.title}"

        presenter.present(notification)
        presenter.present(notification.copy(title = laterTitle))

        val notificationList = shadowOf(notificationManager()).allNotifications

        notificationList shouldHaveSize 1
        notificationList.single().extras.getString(Notification.EXTRA_TITLE) shouldBe laterTitle
    }

    @Test
    fun `TC-2a9d4211-fb15-455a-a888-72323919b7b9 tag가 다른 알림은 서로 대신하지 않는다`() {
        val notification = fixtureMonkey.giveMeOne<RemoteNotification>()

        presenter.present(notification)
        presenter.present(notification.copy(tag = "other ${notification.tag}"))

        shadowOf(notificationManager()).allNotifications shouldHaveSize 2
    }

    @Test
    fun `TC-27fed418-d09e-4354-b82b-ec4e75307dce 지우지 않은 이전 날의 알림은 다음 날 알림이 대신한다`() {
        val previousDay = fixtureMonkey.giveMeOne<RemoteNotification>()
        val nextDay = fixtureMonkey.giveMeOne<RemoteNotification>().copy(title = "next ${previousDay.title}", tag = previousDay.tag)

        presenter.present(previousDay)
        presenter.present(nextDay)

        val notificationList = shadowOf(notificationManager()).allNotifications

        notificationList shouldHaveSize 1
        notificationList.single().extras.getString(Notification.EXTRA_TITLE) shouldBe nextDay.title
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
        // 서버가 2026년 11월 4일에 발생한 일일 메모 알림에 담아 보내는 주소다.
        private const val TIMETABLE_DEEP_LINK: String = "diary://timetable?type=day&date=2026-11-04"

        const val LAUNCHER_ACTIVITY_CLASS_NAME: String = "io.github.taetae98coding.diary.TestLauncherActivity"
    }
}
