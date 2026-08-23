package io.github.taetae98coding.diary.core.notification.impl

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

internal class DailyMemoNotificationWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        applicationContext.notifyDailyMemo()

        return Result.success()
    }
}
