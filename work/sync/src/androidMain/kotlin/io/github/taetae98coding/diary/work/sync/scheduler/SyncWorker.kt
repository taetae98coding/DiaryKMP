package io.github.taetae98coding.diary.work.sync.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CancellationException
import org.koin.android.annotation.KoinWorker
import kotlin.uuid.Uuid

internal const val SYNC_WORK_ACCOUNT_ID_KEY: String = "accountId"

@KoinWorker
internal class SyncWorker(
    context: Context,
    parameters: WorkerParameters,
    private val syncWork: SyncWork,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val accountId =
            inputData
                .getString(SYNC_WORK_ACCOUNT_ID_KEY)
                ?.let { value -> runCatching { Uuid.parse(value) }.getOrNull() }
                ?: return Result.failure()

        return try {
            syncWork.doWork(accountId = accountId)
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            Result.failure()
        }
    }
}
