package io.github.taetae98coding.diary.work.daily.memo

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.Notifier
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyMemoNotificationWorkerTest {
    private lateinit var context: Context
    private lateinit var notifier: Notifier
    private lateinit var notifiedList: MutableList<Notification>

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        notifiedList = mutableListOf()
        notifier = mockk<Notifier>()
        coEvery { notifier.notify(notification = any()) } coAnswers { notifiedList += firstArg<Notification>() }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-001 알림 작업이 실행되면 오늘의 메모를 확인하라는 알림이 하나 발생한다`() {
        doWork() shouldBe ListenableWorker.Result.success()

        notifiedList shouldHaveSize 1
        notifiedList.single().title shouldBe "오늘의 메모를 확인하세요"
    }

    @Test
    @Config(qualifiers = "en")
    fun `기기 언어가 한국어가 아니면 알림 문구가 그 언어로 발생한다`() {
        doWork() shouldBe ListenableWorker.Result.success()

        notifiedList.single().title shouldBe "Check today's memos"
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-004 같은 알림 작업이 여러 번 실행되어도 같은 ID의 알림을 보내 알림이 하나만 남게 한다`() {
        doWork()
        doWork()

        notifiedList shouldHaveSize 2
        notifiedList.map { notification -> notification.id }.distinct() shouldHaveSize 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `알림은 소리와 화면 위 떠오름이 없는 전용 채널로 발생한다`() {
        doWork()

        val channel = notifiedList.single().channel

        channel.id shouldBe DAILY_MEMO_NOTIFICATION_CHANNEL_ID
        channel.isSilent shouldBe true
        channel.name shouldBe "일일 메모 알림"
        channel.description shouldBe "매일 아침 오늘의 메모를 확인하도록 안내합니다."
    }

    private fun doWork(): ListenableWorker.Result {
        val worker =
            TestListenableWorkerBuilder<DailyMemoNotificationWorker>(context)
                .setWorkerFactory(
                    object : WorkerFactory() {
                        override fun createWorker(
                            appContext: Context,
                            workerClassName: String,
                            workerParameters: WorkerParameters,
                        ): ListenableWorker =
                            DailyMemoNotificationWorker(
                                context = appContext,
                                parameters = workerParameters,
                                notifier = notifier,
                            )
                    },
                ).build()

        return runBlocking { worker.doWork() }
    }
}
