package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CancellationException
import org.koin.android.annotation.KoinWorker

@KoinWorker
internal class SyncWorker(
    context: Context,
    parameters: WorkerParameters,
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
}
