package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CancellationException

@AssistedInject
internal class SyncWorker(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val syncWork: SyncWork,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result =
        try {
            syncWork.doWork()
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            Result.failure()
        }

    @AssistedFactory
    fun interface Factory {
        fun create(
            context: Context,
            parameters: WorkerParameters,
        ): SyncWorker
    }
}
