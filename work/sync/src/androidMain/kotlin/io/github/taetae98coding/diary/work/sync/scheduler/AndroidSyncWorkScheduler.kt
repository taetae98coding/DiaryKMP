package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class AndroidSyncWorkScheduler(
    private val context: Context,
) : SyncWorkScheduler {
    override val state: Flow<SyncWorkState>
        get() =
            WorkManager
                .getInstance(context)
                .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
                .map { workInfoList -> workInfoList.toSyncWorkState() }

    override fun sync() {
        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(SYNC_CONSTRAINTS)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build(),
            )
    }

    private fun List<WorkInfo>.toSyncWorkState(): SyncWorkState =
        when {
            any { workInfo -> workInfo.state == WorkInfo.State.RUNNING } -> SyncWorkState.RUNNING
            any { workInfo -> !workInfo.state.isFinished } -> SyncWorkState.PENDING
            else -> SyncWorkState.NONE
        }

    companion object {
        const val SYNC_WORK_NAME: String = "sync"
    }
}
