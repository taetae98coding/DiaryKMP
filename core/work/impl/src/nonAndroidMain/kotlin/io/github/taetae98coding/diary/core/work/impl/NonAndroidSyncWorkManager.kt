package io.github.taetae98coding.diary.core.work.impl

import io.github.taetae98coding.diary.core.work.api.SyncWork
import io.github.taetae98coding.diary.core.work.api.SyncWorkManager
import io.github.taetae98coding.diary.core.work.api.SyncWorkState
import io.github.taetae98coding.diary.core.work.impl.di.SyncWorkScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.time.Duration
import kotlin.uuid.Uuid

@Single
internal class NonAndroidSyncWorkManager(
    private val syncWork: SyncWork,
    @param:SyncWorkScope private val scope: CoroutineScope,
) : SyncWorkManager {
    override val state: Flow<SyncWorkState>
        field = MutableStateFlow(SyncWorkState.NONE)

    private var job: Job? = null
    private var generation: Int = 0

    private var periodicJob: Job? = null
    private var periodicAccountId: Uuid? = null

    override fun sync(accountId: Uuid) {
        job?.cancel()

        val currentGeneration = ++generation
        state.value = SyncWorkState.RUNNING

        job =
            scope.launch {
                try {
                    syncWork.doWork(accountId = accountId)
                } finally {
                    if (currentGeneration == generation) {
                        state.value = SyncWorkState.NONE
                    }
                }
            }
    }

    override fun schedulePeriodicSync(
        accountId: Uuid,
        period: Duration,
    ) {
        periodicAccountId = accountId

        if (periodicJob?.isActive == true) return

        periodicJob = scope.launch { syncEveryPeriod(period = period) }
    }

    override fun cancelPeriodicSync() {
        periodicAccountId = null
        periodicJob?.cancel()
        periodicJob = null
    }

    private suspend fun syncEveryPeriod(period: Duration) {
        while (currentCoroutineContext().isActive) {
            delay(period)

            val accountId = periodicAccountId ?: break

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
