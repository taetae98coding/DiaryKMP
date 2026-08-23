package io.github.taetae98coding.diary.core.notification.impl

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Instant

private val SEOUL = TimeZone.of("Asia/Seoul")
private val EIGHT_AM = LocalTime(hour = 8, minute = 0)
private const val ONE_HOUR_SECONDS = 60L * 60
private const val ONE_DAY_MILLIS = 24 * ONE_HOUR_SECONDS * 1000

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidDailyMemoNotificationSchedulerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()

        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration
                .Builder()
                .setExecutor(SynchronousExecutor())
                .build(),
        )
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-002 앱이 시작하면 하루마다 반복하는 알림 작업이 하나 예약된다`() {
        schedule(now = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL))

        val workInfoList = workInfoList()

        workInfoList shouldHaveSize 1
        workInfoList.single().periodicityInfo?.repeatIntervalMillis shouldBe ONE_DAY_MILLIS
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-FEATURE-001 첫 알림은 다음에 오는 오전 8시로 예약된다`() {
        val enqueuedAt = System.currentTimeMillis()

        schedule(now = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL))

        scheduledAfterSeconds(enqueuedAt = enqueuedAt) shouldBe 2 * ONE_HOUR_SECONDS
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-003 오전 8시를 지나 시작하면 첫 알림은 다음 날 오전 8시로 예약된다`() {
        val enqueuedAt = System.currentTimeMillis()

        schedule(now = LocalDateTime(2026, 9, 8, 10, 0).toInstant(SEOUL))

        scheduledAfterSeconds(enqueuedAt = enqueuedAt) shouldBe 22 * ONE_HOUR_SECONDS
    }

    @Test
    fun `TC-DAILY-MEMO-NOTIFICATION-DOMAIN-006 예약을 다시 요청해도 예약은 하나로 유지되고 발생 시각이 앞당겨지지 않는다`() {
        val enqueuedAt = System.currentTimeMillis()

        schedule(now = LocalDateTime(2026, 9, 8, 6, 0).toInstant(SEOUL))
        schedule(now = LocalDateTime(2026, 9, 8, 7, 30).toInstant(SEOUL))

        val workInfoList = workInfoList()

        workInfoList shouldHaveSize 1
        scheduledAfterSeconds(enqueuedAt = enqueuedAt) shouldBe 2 * ONE_HOUR_SECONDS
    }

    // 작업을 넣는 동안 시스템 시각이 밀리초 단위로 흐르므로 예약이 정하는 정밀도인 초까지만 비교한다.
    private fun scheduledAfterSeconds(enqueuedAt: Long): Long = (workInfoList().single().nextScheduleTimeMillis - enqueuedAt) / 1000

    private fun schedule(now: Instant) {
        val scheduler =
            AndroidDailyMemoNotificationScheduler(
                context = context,
                clock =
                    object : Clock {
                        override fun now(): Instant = now
                    },
                currentTimeZone = { SEOUL },
            )

        runBlocking { scheduler.schedule(time = EIGHT_AM) }
    }

    private fun workInfoList(): List<WorkInfo> =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(DAILY_MEMO_NOTIFICATION_WORK_NAME)
            .get()
}
