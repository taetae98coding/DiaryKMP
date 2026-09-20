package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.di.SyncScope
import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.time.Duration
import kotlin.uuid.Uuid

@Single
internal class CoroutinePeriodicSyncWorkScheduler(
    private val syncWork: SyncWork,
    @param:SyncScope private val scope: CoroutineScope,
) : PeriodicSyncWorkScheduler {
    private var job: Job? = null
    private var accountId: Uuid? = null

    override fun schedule(
        accountId: Uuid,
        period: Duration,
    ) {
        this.accountId = accountId

        if (job?.isActive == true) return

        job = scope.launch { syncEveryPeriod(period = period) }
    }

    override fun cancel() {
        accountId = null
        job?.cancel()
        job = null
    }

    private suspend fun syncEveryPeriod(period: Duration) {
        while (currentCoroutineContext().isActive) {
            delay(period)

            val accountId = accountId ?: break

            try {
                syncWork.doWork(accountId = accountId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Throwable) {
                // 한 주기의 실패로 예약이 끝나면 다음 주기가 실행되지 않으므로, 실패를 삼키고 다음 주기를 기다린다.
            }
        }
    }
}
