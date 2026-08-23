package io.github.taetae98coding.diary.core.notification.impl

import io.github.taetae98coding.diary.core.notification.api.DailyMemoNotificationScheduler
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

internal class TimerDailyMemoNotificationScheduler(
    private val clock: Clock,
    private val notifier: DailyMemoNotifier,
    private val scope: CoroutineScope,
    private val currentTimeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : DailyMemoNotificationScheduler {
    private var job: Job? = null

    // 기다리고 있는 예약이 있으면 그대로 두어야 예약이 하나로 유지되고 발생 시각이 앞당겨지지 않는다.
    override suspend fun schedule(time: LocalTime) {
        if (job?.isActive == true) return

        job = scope.launch { notifyEveryDay(time = time) }
    }

    private suspend fun notifyEveryDay(time: LocalTime) {
        var from = clock.now()

        while (currentCoroutineContext().isActive) {
            val next = nextDailyMemoNotificationInstant(from = from, time = time, timeZone = currentTimeZone())

            delay(next - clock.now())
            notifier.notifyDailyMemo()

            // 방금 알린 시각이 다음 후보로 다시 뽑히지 않도록 그 시각 바로 뒤에서 다음 시각을 찾는다.
            from = next + 1.milliseconds
        }
    }
}
