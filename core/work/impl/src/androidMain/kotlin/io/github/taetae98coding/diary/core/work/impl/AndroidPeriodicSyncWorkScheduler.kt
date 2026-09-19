package io.github.taetae98coding.diary.core.work.impl

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.github.taetae98coding.diary.core.work.api.PeriodicSyncWorkScheduler
import org.koin.core.annotation.Factory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.uuid.Uuid

@Factory
internal class AndroidPeriodicSyncWorkScheduler(
    private val context: Context,
) : PeriodicSyncWorkScheduler {
    override fun schedule(
        accountId: Uuid,
        period: Duration,
    ) {
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<SyncWorker>(period.inWholeSeconds, TimeUnit.SECONDS)
                    .setConstraints(SYNC_CONSTRAINTS)
                    .setInitialDelay(period.inWholeSeconds, TimeUnit.SECONDS)
                    .setInputData(workDataOf(SYNC_WORK_ACCOUNT_ID_KEY to accountId.toString()))
                    .build(),
            )
    }

    override fun cancel() {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }

    companion object {
        const val PERIODIC_SYNC_WORK_NAME: String = "periodicSync"
    }
}
