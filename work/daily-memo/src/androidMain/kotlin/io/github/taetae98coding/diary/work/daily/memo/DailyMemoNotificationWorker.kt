package io.github.taetae98coding.diary.work.daily.memo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.taetae98coding.diary.notification.Notification
import io.github.taetae98coding.diary.notification.NotificationChannel
import io.github.taetae98coding.diary.notification.Notifier
import org.koin.android.annotation.KoinWorker

@KoinWorker
internal class DailyMemoNotificationWorker(
    context: Context,
    parameters: WorkerParameters,
    private val notifier: Notifier,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        notifier.notify(notification = applicationContext.dailyMemoNotification())

        return Result.success()
    }
}

internal fun Context.dailyMemoNotification(): Notification =
    Notification(
        id = DAILY_MEMO_NOTIFICATION_ID,
        channel =
            NotificationChannel(
                id = DAILY_MEMO_NOTIFICATION_CHANNEL_ID,
                name = getString(R.string.daily_memo_notification_channel_name),
                description = getString(R.string.daily_memo_notification_channel_description),
                isSilent = true,
            ),
        title = getString(R.string.daily_memo_notification_title),
    )
