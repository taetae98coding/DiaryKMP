package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.domain.memo.usecase.GetDailyMemoUseCase
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.Notifier
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal class DailyMemoNotificationWork(
    private val getDailyMemoUseCase: GetDailyMemoUseCase,
    private val notifier: Notifier,
    private val clock: Clock,
    private val createNotification: (DailyMemoNotificationContent) -> Notification,
    private val currentTimeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    suspend fun doWork() {
        val today = clock.now().toLocalDateTime(currentTimeZone()).date
        val content =
            getDailyMemoUseCase(parameter = today)
                .first()
                .fold(
                    onSuccess = { memoList -> DailyMemoNotificationContent.Loaded(memoList = memoList) },
                    // 메모를 가져오지 못해도 알림은 보내야 하므로, 내용을 정할 수 없는 환경과 같은 안내로 대신한다.
                    onFailure = { DailyMemoNotificationContent.Unavailable },
                )

        notifier.notify(notification = createNotification(content))
    }
}
