package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncWorkScheduler
import org.koin.core.annotation.Factory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@Factory
internal class AndroidPeriodicSyncWorkScheduler(
    private val context: Context,
) : PeriodicSyncWorkScheduler {
    override fun schedule(period: Duration) {
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SyncWorker>(period.inWholeSeconds, TimeUnit.SECONDS)
                    .setConstraints(SYNC_CONSTRAINTS)
                    .setInitialDelay(period.inWholeSeconds, TimeUnit.SECONDS)
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
