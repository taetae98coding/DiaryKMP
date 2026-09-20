package io.github.taetae98coding.diary.work.daily.memo

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

internal const val DAILY_MEMO_NOTIFICATION_WORK_NAME: String = "dailyMemoNotification"

internal class AndroidDailyMemoNotificationManager(
    private val context: Context,
    private val clock: Clock,
    private val currentTimeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : DailyMemoNotificationManager {
    override suspend fun schedule(time: LocalTime) {
        val delay = dailyMemoNotificationDelay(from = clock.now(), time = time, timeZone = currentTimeZone())

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_MEMO_NOTIFICATION_WORK_NAME,
                // 이미 예약이 있으면 그대로 두어야 앱을 다시 시작해도 예약이 하나로 유지되고 발생 시각이 앞당겨지지 않는다.
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<DailyMemoNotificationWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(delay.inWholeSeconds, TimeUnit.SECONDS)
                    .build(),
            )
    }

    // Android는 발생 시점에 Worker가 오늘의 메모를 정하므로 미리 정한 내용을 쓰지 않는다.
    override suspend fun submitUpcoming(
        time: LocalTime,
        upcomingList: List<UpcomingDailyMemoNotification>,
    ) = Unit
}
