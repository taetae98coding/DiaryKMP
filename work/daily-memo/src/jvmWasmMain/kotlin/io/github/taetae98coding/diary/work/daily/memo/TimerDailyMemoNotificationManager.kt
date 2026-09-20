package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

internal class TimerDailyMemoNotificationManager(
    private val clock: Clock,
    private val work: DailyMemoNotificationWork,
    private val scope: CoroutineScope,
    private val currentTimeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : DailyMemoNotificationManager {
    private var job: Job? = null

    // 기다리고 있는 예약이 있으면 그대로 두어야 예약이 하나로 유지되고 발생 시각이 앞당겨지지 않는다.
    override suspend fun schedule(time: LocalTime) {
        if (job?.isActive == true) return

        job = scope.launch { notifyEveryDay(time = time) }
    }

    // JVM 데스크톱과 웹은 발생 시점에 타이머가 오늘의 메모를 정하므로 미리 정한 내용을 쓰지 않는다.
    override suspend fun submitUpcoming(
        time: LocalTime,
        upcomingList: List<UpcomingDailyMemoNotification>,
    ) = Unit

    private suspend fun notifyEveryDay(time: LocalTime) {
        var from: Instant = clock.now()

        while (currentCoroutineContext().isActive) {
            val next = nextDailyMemoNotificationInstant(from = from, time = time, timeZone = currentTimeZone())

            delay(next - clock.now())
            work.doWork()

            // 방금 알린 시각이 다음 후보로 다시 뽑히지 않도록 그 시각 바로 뒤에서 다음 시각을 찾는다.
            from = next + 1.milliseconds
        }
    }
}
