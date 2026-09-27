package io.github.taetae98coding.diary.work.fileupload.report

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.work.file.upload.R
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko")
class AndroidFileUploadNotifierTest {
    private lateinit var context: Context
    private lateinit var notifier: AndroidFileUploadNotifier

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        notifier = AndroidFileUploadNotifier(context = context)
        addLauncherActivity()
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-001 올리는 동안 파일 이름과 진행을 보여 주는 진행 중 알림을 만든다`() {
        mapOf<Int?, Pair<Int, Boolean>>(
            null to (0 to true),
            40 to (40 to false),
        ).forEach { (percent, expected) ->
            val name = "보고서-${fixtureMonkey.giveMeOne<Int>()}.pdf"

            val notification = notifier.createForegroundInfo(name = name, percent = percent).notification

            notification.extras.getString(Notification.EXTRA_TITLE) shouldBe "파일 올리는 중"
            notification.extras.getString(Notification.EXTRA_TEXT) shouldBe name
            notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX) shouldBe 100
            notification.extras.getInt(Notification.EXTRA_PROGRESS) shouldBe expected.first
            notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE) shouldBe expected.second
            (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) shouldBe true
            notification.channelId shouldBe FILE_UPLOAD_NOTIFICATION_CHANNEL_ID
            notification.smallIcon.resId shouldBe R.drawable.ic_file_upload_notification
        }
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-004 결과마다 정한 제목과 본문의 결과 알림을 보낸다`() {
        val name = "보고서-${fixtureMonkey.giveMeOne<Int>()}.pdf"

        mapOf(
            FileUploadResult.Succeeded(name = name, fileId = fixtureMonkey.giveMeOne<Uuid>()) to (SUCCEEDED_TITLE to name),
            FileUploadResult.TooLarge to (FAILED_TITLE to "50MB 이하 파일만 올릴 수 있습니다."),
            FileUploadResult.Failed(name = name) to (FAILED_TITLE to name),
        ).forEach { (result, expected) ->
            notifier.notifyResult(result = result)

            val posted = shadowOf(notificationManager()).getNotification(FILE_UPLOAD_RESULT_NOTIFICATION_ID).shouldNotBeNull()
            posted.extras.getString(Notification.EXTRA_TITLE) shouldBe expected.first
            posted.extras.getString(Notification.EXTRA_TEXT) shouldBe expected.second
            (posted.flags and Notification.FLAG_AUTO_CANCEL != 0) shouldBe true
        }
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-007 앞선 결과 알림이 남아 있을 때 다른 결과를 알리면 새 결과 하나만 남는다`() {
        val name = "memo-${fixtureMonkey.giveMeOne<Int>()}.txt"

        notifier.notifyResult(result = FileUploadResult.Failed(name = fileName()))
        notifier.notifyResult(result = FileUploadResult.Succeeded(name = name, fileId = fixtureMonkey.giveMeOne<Uuid>()))

        val posted = shadowOf(notificationManager()).allNotifications
        posted.size shouldBe 1
        posted.single().extras.getString(Notification.EXTRA_TITLE) shouldBe SUCCEEDED_TITLE
        posted.single().extras.getString(Notification.EXTRA_TEXT) shouldBe name
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-008 알림을 선택하면 FileHome 주소를 담아 앱의 진입 화면을 연다`() {
        notifier.notifyResult(result = FileUploadResult.TooLarge)

        val intent = shadowOf(shadowOf(notificationManager()).allNotifications.single().contentIntent).savedIntent

        intent.dataString shouldBe FILE_HOME_DEEP_LINK
        intent.component?.className shouldBe LAUNCHER_ACTIVITY_CLASS_NAME
        (intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0) shouldBe true
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-014 진행 알림은 선택해도 사라지지 않고 FileHome을 연다`() {
        val notification = notifier.createForegroundInfo(name = fileName(), percent = null).notification

        (notification.flags and Notification.FLAG_AUTO_CANCEL != 0) shouldBe false
        (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) shouldBe true
        shadowOf(notification.contentIntent).savedIntent.dataString shouldBe FILE_HOME_DEEP_LINK
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-FEATURE-015 이름을 알 수 없는 파일의 실패 결과 알림은 제목만 담는다`() {
        notifier.notifyResult(result = FileUploadResult.Failed(name = ""))

        val posted = shadowOf(notificationManager()).getNotification(FILE_UPLOAD_RESULT_NOTIFICATION_ID).shouldNotBeNull()
        posted.extras.getString(Notification.EXTRA_TITLE) shouldBe FAILED_TITLE
        posted.extras.getCharSequence(Notification.EXTRA_TEXT).shouldBeNull()
    }

    @Test
    fun `TC-FILE-UPLOAD-NOTIFICATION-DOMAIN-004 진행 알림과 결과 알림은 일일 메모 알림과 다른 전용 알림 설정 항목으로 표시된다`() {
        val progress = notifier.createForegroundInfo(name = fileName(), percent = null).notification
        notifier.notifyResult(result = FileUploadResult.TooLarge)
        val result = shadowOf(notificationManager()).getNotification(FILE_UPLOAD_RESULT_NOTIFICATION_ID).shouldNotBeNull()

        progress.channelId shouldBe FILE_UPLOAD_NOTIFICATION_CHANNEL_ID
        result.channelId shouldBe FILE_UPLOAD_NOTIFICATION_CHANNEL_ID
        FILE_UPLOAD_NOTIFICATION_CHANNEL_ID shouldNotBe DAILY_MEMO_NOTIFICATION_CHANNEL_ID
        notificationManager().getNotificationChannel(FILE_UPLOAD_NOTIFICATION_CHANNEL_ID).shouldNotBeNull()
    }

    @Test
    fun `알림을 보내면 소리와 떠오름이 없는 파일 올리기 전용 채널을 만든다`() {
        notifier.notifyResult(result = FileUploadResult.TooLarge)

        val channel = notificationManager().getNotificationChannel(FILE_UPLOAD_NOTIFICATION_CHANNEL_ID).shouldNotBeNull()
        channel.importance shouldBe NotificationManager.IMPORTANCE_LOW
        channel.name.toString() shouldBe "파일 올리기"
        channel.description shouldBe "파일을 올리는 동안의 진행과 결과를 알려 줍니다."
    }

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
        const val SUCCEEDED_TITLE: String = "파일을 올렸습니다"
        const val FAILED_TITLE: String = "파일을 올리지 못했습니다"
        const val FILE_HOME_DEEP_LINK: String = "diary://file-home"

        // 일일 메모 알림이 쓰는 채널의 이름이다. 그 모듈을 참조하지 않으므로 값을 직접 적는다.
        const val DAILY_MEMO_NOTIFICATION_CHANNEL_ID: String = "dailyMemo"
    }
}

private fun fileName(): String = "file-${fixtureMonkey.giveMeOne<Int>()}.txt"
