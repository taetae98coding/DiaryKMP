package io.github.taetae98coding.diary.work.daily.memo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.NotificationChannel
import org.koin.android.annotation.KoinWorker

@KoinWorker
internal class DailyMemoNotificationWorker(
    context: Context,
    parameters: WorkerParameters,
    private val work: DailyMemoNotificationWork,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        work.doWork()

        return Result.success()
    }
}

internal fun Context.dailyMemoNotification(content: DailyMemoNotificationContent): Notification =
    Notification(
        id = DAILY_MEMO_NOTIFICATION_ID,
        channel =
            NotificationChannel(
                id = DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
                name = getString(R.string.daily_memo_notification_channel_name),
                description = getString(R.string.daily_memo_notification_channel_description),
                isSilent = true,
            ),
        title = dailyMemoNotificationTitle(content = content),
        body = dailyMemoNotificationBody(content = content),
    )

private fun Context.dailyMemoNotificationTitle(content: DailyMemoNotificationContent): String =
    when (content) {
        is DailyMemoNotificationContent.Loaded -> {
            val count = content.memoList.size

            if (count == 0) {
                getString(R.string.daily_memo_notification_title_empty)
            } else {
                resources.getQuantityString(R.plurals.daily_memo_notification_title_count, count, count)
            }
        }

        DailyMemoNotificationContent.Unavailable -> getString(R.string.daily_memo_notification_title)
    }
