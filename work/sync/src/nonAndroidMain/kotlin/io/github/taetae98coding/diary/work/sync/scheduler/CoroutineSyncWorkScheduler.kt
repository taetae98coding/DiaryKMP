package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.di.SyncScope
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.scheduler.SyncWorkState
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
internal class CoroutineSyncWorkScheduler(
    private val syncWork: SyncWork,
    @param:SyncScope private val scope: CoroutineScope,
) : SyncWorkScheduler {
    override val state: Flow<SyncWorkState>
        field = MutableStateFlow(SyncWorkState.NONE)

    private var job: Job? = null
    private var generation: Int = 0

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
}
