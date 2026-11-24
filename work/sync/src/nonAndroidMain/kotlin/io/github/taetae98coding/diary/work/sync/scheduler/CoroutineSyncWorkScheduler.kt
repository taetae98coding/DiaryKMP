package io.github.taetae98coding.diary.work.sync.scheduler

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.work.sync.di.SyncScope
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class CoroutineSyncWorkScheduler(
    private val syncWork: SyncWork,
    @param:SyncScope private val scope: CoroutineScope,
) : SyncWorkScheduler {
    override val state: Flow<SyncWorkState>
        field = MutableStateFlow(SyncWorkState.NONE)

    private var job: Job? = null
    private var generation: Int = 0

    override fun sync() {
        job?.cancel()

        val currentGeneration = ++generation
        state.value = SyncWorkState.RUNNING

        job =
            scope.launch {
                try {
                    syncWork.doWork()
                } finally {
                    if (currentGeneration == generation) {
                        state.value = SyncWorkState.NONE
                    }
                }
            }
    }
}
