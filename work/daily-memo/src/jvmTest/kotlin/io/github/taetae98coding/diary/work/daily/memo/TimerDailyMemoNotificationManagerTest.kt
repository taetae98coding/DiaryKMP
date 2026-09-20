@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.daily.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.domain.memo.usecase.GetDailyMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.Notifier
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

private val SEOUL = TimeZone.of("Asia/Seoul")
private val EIGHT_AM = LocalTime(hour = 8, minute = 0)

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class TimerDailyMemoNotificationManagerTest :
    FunSpec({
        test("TC-DAILY-MEMO-NOTIFICATION-FEATURE-001 오전 8시가 되면 알림이 발생한다") {
            runTest {
                val notifiedInstantList = mutableListOf<Instant>()
                val clock = testClock(start = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL))

                scheduler(clock = clock, notifiedInstantList = notifiedInstantList, scope = backgroundScope).schedule(time = EIGHT_AM)

                advanceTimeBy(2.hours)
                notifiedInstantList.shouldBeEmpty()

                runCurrent()
                notifiedInstantList shouldBe listOf(LocalDateTime(2026, 9, 8, 8, 0).toInstant(SEOUL))
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-FEATURE-002 알림은 매일 오전 8시마다 반복해서 발생한다") {
            runTest {
                val notifiedInstantList = mutableListOf<Instant>()
                val clock = testClock(start = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL))

                scheduler(clock = clock, notifiedInstantList = notifiedInstantList, scope = backgroundScope).schedule(time = EIGHT_AM)

                advanceTimeBy(2.hours + 2.days)
                runCurrent()

                notifiedInstantList shouldBe
                    listOf(
                        LocalDateTime(2026, 9, 8, 8, 0).toInstant(SEOUL),
                        LocalDateTime(2026, 9, 9, 8, 0).toInstant(SEOUL),
                        LocalDateTime(2026, 9, 10, 8, 0).toInstant(SEOUL),
                    )
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-003 앱이 오전 8시를 지나 시작하면 그날은 알리지 않고 다음 날 오전 8시에 알린다") {
            runTest {
                val notifiedInstantList = mutableListOf<Instant>()
                val clock = testClock(start = LocalDateTime(2026, 9, 8, 10, 0).toInstant(SEOUL))

                scheduler(clock = clock, notifiedInstantList = notifiedInstantList, scope = backgroundScope).schedule(time = EIGHT_AM)

                advanceTimeBy(1.days)
                runCurrent()

                notifiedInstantList shouldBe listOf(LocalDateTime(2026, 9, 9, 8, 0).toInstant(SEOUL))
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-006 정확히 오전 8시에 시작해도 그 시각의 알림은 한 번만 발생한다") {
            runTest {
                val notifiedInstantList = mutableListOf<Instant>()
                val clock = testClock(start = LocalDateTime(2026, 9, 8, 8, 0).toInstant(SEOUL))

                scheduler(clock = clock, notifiedInstantList = notifiedInstantList, scope = backgroundScope).schedule(time = EIGHT_AM)

                advanceTimeBy(1.hours)
                runCurrent()

                notifiedInstantList shouldBe listOf(LocalDateTime(2026, 9, 8, 8, 0).toInstant(SEOUL))
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-006 TC-DAILY-MEMO-NOTIFICATION-DOMAIN-007 예약을 다시 요청해도 알림은 원래 예약된 오전 8시에 한 번만 발생한다") {
            runTest {
                val notifiedInstantList = mutableListOf<Instant>()
                val clock = testClock(start = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL))
                val scheduler = scheduler(clock = clock, notifiedInstantList = notifiedInstantList, scope = backgroundScope)

                scheduler.schedule(time = EIGHT_AM)
                advanceTimeBy(1.hours)
                scheduler.schedule(time = EIGHT_AM)

                advanceTimeBy(1.hours)
                runCurrent()

                notifiedInstantList shouldBe listOf(LocalDateTime(2026, 9, 8, 8, 0).toInstant(SEOUL))
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-002 알림을 알린 뒤 시간대가 바뀌면 다음 알림은 바뀐 시간대의 오전 8시에 발생한다") {
            runTest {
                val notifiedInstantList = mutableListOf<Instant>()
                val clock = testClock(start = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL))
                var timeZone = SEOUL

                scheduler(
                    clock = clock,
                    notifiedInstantList = notifiedInstantList,
                    scope = backgroundScope,
                    currentTimeZone = { timeZone },
                ).schedule(time = EIGHT_AM)

                // 대기 중인 알림은 이미 정해진 시각을 따르므로, 다음 시각을 정하기 전에 시간대를 바꾼다.
                advanceTimeBy(2.hours)
                timeZone = TimeZone.UTC
                runCurrent()
                advanceTimeBy(1.days)
                runCurrent()

                notifiedInstantList shouldBe
                    listOf(
                        LocalDateTime(2026, 9, 8, 8, 0).toInstant(SEOUL),
                        LocalDateTime(2026, 9, 8, 8, 0).toInstant(TimeZone.UTC),
                    )
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-014 알림 내용은 알림이 발생하는 시점의 메모로 정한다") {
            runTest {
                val today = LocalDate(2026, 9, 8)
                val firstMemo = fixtureMonkey.giveMeOne<DailyMemo>()
                val secondMemo = fixtureMonkey.giveMeOne<DailyMemo>()
                val memoListFlow = MutableStateFlow(Result.success(listOf(firstMemo)))
                val getDailyMemoUseCase = mockk<GetDailyMemoUseCase>()
                every { getDailyMemoUseCase(parameter = today) } returns memoListFlow
                val notifiedList = mutableListOf<Notification>()
                val notifier = mockk<Notifier>()
                coEvery { notifier.notify(notification = any()) } coAnswers { notifiedList += firstArg<Notification>() }
                val clock = testClock(start = LocalDateTime(date = today, time = LocalTime(hour = 6, minute = 0)).toInstant(SEOUL))
                val work =
                    DailyMemoNotificationWork(
                        getDailyMemoUseCase = getDailyMemoUseCase,
                        notifier = notifier,
                        clock = clock,
                        createNotification = { content -> fixtureMonkey.giveMeOne<Notification>().copy(body = dailyMemoNotificationBody(content = content)) },
                        currentTimeZone = { SEOUL },
                    )

                TimerDailyMemoNotificationManager(clock = clock, work = work, scope = backgroundScope, currentTimeZone = { SEOUL }).schedule(time = EIGHT_AM)

                advanceTimeBy(1.hours)
                memoListFlow.value = Result.success(listOf(firstMemo, secondMemo))
                advanceTimeBy(1.hours)
                runCurrent()

                notifiedList.single().body shouldBe listOf(firstMemo, secondMemo).joinToString(separator = "\n") { memo -> "- ${memo.title}" }
            }
        }
    })

private fun TestScope.testClock(start: Instant): Clock {
    val clock = mockk<Clock>()
    every { clock.now() } answers { start + testScheduler.currentTime.milliseconds }

    return clock
}

private fun scheduler(
    clock: Clock,
    notifiedInstantList: MutableList<Instant>,
    scope: CoroutineScope,
    currentTimeZone: () -> TimeZone = { SEOUL },
): TimerDailyMemoNotificationManager {
    val work = mockk<DailyMemoNotificationWork>()
    coEvery { work.doWork() } coAnswers { notifiedInstantList += clock.now() }

    return TimerDailyMemoNotificationManager(
        clock = clock,
        work = work,
        scope = scope,
        currentTimeZone = currentTimeZone,
    )
}
