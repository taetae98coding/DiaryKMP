package io.github.taetae98coding.diary.work.daily.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.domain.memo.usecase.GetDailyMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.Notifier
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Instant

private val SEOUL = TimeZone.of("Asia/Seoul")
private val TODAY = LocalDate(2026, 9, 8)

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class DailyMemoNotificationWorkTest :
    FunSpec({
        test("TC-DAILY-MEMO-NOTIFICATION-FEATURE-005 오늘의 메모가 있으면 알림 내용에 그 메모들이 순서대로 담기고 본문은 제목을 한 줄에 하나씩 나열한다") {
            val memoList = List(size = 3) { dailyMemo() }
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = TODAY) } returns flowOf(Result.success(memoList))
            val notifiedList = mutableListOf<Notification>()
            val contentList = mutableListOf<DailyMemoNotificationContent>()

            work(
                getDailyMemoUseCase = getDailyMemoUseCase,
                notifiedList = notifiedList,
                contentList = contentList,
                now = LocalDateTime(date = TODAY, time = EIGHT_AM).toInstant(SEOUL),
            ).doWork()

            contentList shouldBe listOf(DailyMemoNotificationContent.Loaded(memoList = memoList))
            notifiedList.single().body shouldBe memoList.joinToString(separator = "\n") { memo -> "- ${memo.title}" }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-FEATURE-006 오늘의 메모가 없으면 알림 내용은 빈 목록이고 본문을 두지 않는다") {
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = TODAY) } returns flowOf(Result.success(emptyList()))
            val notifiedList = mutableListOf<Notification>()
            val contentList = mutableListOf<DailyMemoNotificationContent>()

            work(
                getDailyMemoUseCase = getDailyMemoUseCase,
                notifiedList = notifiedList,
                contentList = contentList,
                now = LocalDateTime(date = TODAY, time = EIGHT_AM).toInstant(SEOUL),
            ).doWork()

            contentList shouldBe listOf(DailyMemoNotificationContent.Loaded(memoList = emptyList()))
            notifiedList.single().body shouldBe ""
        }

        test("TC-DAILY-MEMO-NOTIFICATION-FEATURE-007 오늘의 메모를 가져오지 못하면 내용을 정할 수 없는 알림을 한 번 보낸다") {
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = TODAY) } returns flowOf(Result.failure(IllegalStateException("query error")))
            val notifiedList = mutableListOf<Notification>()
            val contentList = mutableListOf<DailyMemoNotificationContent>()

            work(
                getDailyMemoUseCase = getDailyMemoUseCase,
                notifiedList = notifiedList,
                contentList = contentList,
                now = LocalDateTime(date = TODAY, time = EIGHT_AM).toInstant(SEOUL),
            ).doWork()

            contentList shouldBe listOf(DailyMemoNotificationContent.Unavailable)
            notifiedList shouldHaveSize 1
            notifiedList.single().body shouldBe ""
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-016 알림이 발생하는 날은 발생 시점의 기기 현지 날짜다") {
            val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
            every { getDailyMemoUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
            // UTC로는 아직 9월 8일 밤이지만 서울에서는 9월 9일 아침이다.
            val now = LocalDateTime(2026, 9, 8, 23, 0).toInstant(TimeZone.UTC)

            work(
                getDailyMemoUseCase = getDailyMemoUseCase,
                notifiedList = mutableListOf(),
                contentList = mutableListOf(),
                now = now,
            ).doWork()

            verify(exactly = 1) { getDailyMemoUseCase(parameter = LocalDate(2026, 9, 9)) }
        }

        test("알림 본문은 메모 제목을 저장된 그대로 쓴다") {
            val memo = dailyMemo()
            val content = DailyMemoNotificationContent.Loaded(memoList = listOf(memo))

            dailyMemoNotificationBody(content = content) shouldBe "- ${memo.title}"
        }
    })

private val EIGHT_AM = kotlinx.datetime.LocalTime(hour = 8, minute = 0)

// 생성한 문자열은 빈 값일 수 있어, 제목 줄이 비어 보이지 않도록 고정 접두어를 붙인다.
private fun dailyMemo(): DailyMemo = fixtureMonkey.giveMeOne<DailyMemo>().let { memo -> memo.copy(title = "title ${memo.title}") }

private fun work(
    getDailyMemoUseCase: GetDailyMemoUseCase,
    notifiedList: MutableList<Notification>,
    contentList: MutableList<DailyMemoNotificationContent>,
    now: Instant,
): DailyMemoNotificationWork {
    val notifier = mockk<Notifier>()
    coEvery { notifier.notify(notification = any()) } coAnswers { notifiedList += firstArg<Notification>() }
    val clock = mockk<Clock>()
    every { clock.now() } returns now

    return DailyMemoNotificationWork(
        getDailyMemoUseCase = getDailyMemoUseCase,
        notifier = notifier,
        clock = clock,
        createNotification = { content ->
            contentList += content
            fixtureMonkey.giveMeOne<Notification>().copy(body = dailyMemoNotificationBody(content = content))
        },
        currentTimeZone = { SEOUL },
    )
}
