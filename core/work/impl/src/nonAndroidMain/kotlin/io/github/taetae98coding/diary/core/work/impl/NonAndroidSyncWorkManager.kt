package io.github.taetae98coding.diary.core.work.impl

import io.github.taetae98coding.diary.core.work.api.SyncWork
import io.github.taetae98coding.diary.core.work.api.SyncWorkManager
import io.github.taetae98coding.diary.core.work.api.SyncWorkState
import io.github.taetae98coding.diary.core.work.impl.di.SyncWorkScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
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
