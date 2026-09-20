package io.github.taetae98coding.diary.work.daily.memo

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.domain.memo.usecase.GetDailyMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.Notifier
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.time.Clock

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

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
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-001 알림 작업이 실행되면 알림이 하나 발생한다`() {
        doWork(memoList(count = 1)) shouldBe ListenableWorker.Result.success()

        notifiedList shouldHaveSize 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-005 오늘의 메모가 있으면 개수를 담은 제목과 제목 목록 본문으로 알림이 발생한다`() {
        val memoList = List(size = 3) { index -> DailyMemo(id = fixtureMonkey.giveMeOne(), title = "메모 $index") }

        doWork(Result.success(memoList))

        notifiedList.single().title shouldBe "오늘 확인할 메모가 3개 있어요"
        notifiedList.single().body shouldBe "- 메모 0\n- 메모 1\n- 메모 2"
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-006 오늘의 메모가 없으면 없다는 안내만 담고 본문을 두지 않는다`() {
        doWork(Result.success(emptyList()))

        notifiedList.single().title shouldBe "오늘 확인할 메모가 없어요"
        notifiedList.single().body shouldBe ""
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-007 오늘의 메모를 가져오지 못하면 확인 안내만 담아 알림을 보낸다`() {
        doWork(Result.failure(IllegalStateException("query error"))) shouldBe ListenableWorker.Result.success()

        notifiedList.single().title shouldBe "오늘의 메모를 확인하세요"
        notifiedList.single().body shouldBe ""
    }

    @Test
    @Config(qualifiers = "en")
    fun `기기 언어가 한국어가 아니면 알림 문구가 그 언어로 발생하고 개수가 하나면 단수형을 쓴다`() {
        doWork(memoList(count = 1))
        doWork(memoList(count = 2))
        doWork(Result.success(emptyList()))
        doWork(Result.failure(IllegalStateException("query error")))

        notifiedList.map { notification -> notification.title } shouldBe
            listOf(
                "You have 1 memo to check today",
                "You have 2 memos to check today",
                "No memos to check today",
                "Check today's memos",
            )
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-004 같은 알림 작업이 여러 번 실행되어도 같은 ID의 알림을 보내 알림이 하나만 남게 한다`() {
        doWork(memoList(count = 1))
        doWork(memoList(count = 2))

        notifiedList shouldHaveSize 2
        notifiedList.map { notification -> notification.id }.distinct() shouldHaveSize 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `알림은 소리와 화면 위 떠오름이 없는 전용 채널로 발생한다`() {
        doWork(memoList(count = 1))

        val channel = notifiedList.single().channel

        channel.id shouldBe DAILY_MEMO_NOTIFICATION_CHANNEL_ID
        channel.isSilent shouldBe true
        channel.name shouldBe "일일 메모 알림"
        channel.description shouldBe "매일 아침 오늘의 메모를 확인하도록 안내합니다."
    }

    private fun memoList(count: Int): Result<List<DailyMemo>> = Result.success(List(size = count) { fixtureMonkey.giveMeOne<DailyMemo>() })

    private fun doWork(result: Result<List<DailyMemo>>): ListenableWorker.Result {
        val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
        every { getDailyMemoUseCase(parameter = any()) } returns flowOf(result)
        val work =
            DailyMemoNotificationWork(
                getDailyMemoUseCase = getDailyMemoUseCase,
                notifier = notifier,
                clock = Clock.System,
                createNotification = context::dailyMemoNotification,
            )
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
                                work = work,
                            )
                    },
                ).build()

        return runBlocking { worker.doWork() }
    }
}
