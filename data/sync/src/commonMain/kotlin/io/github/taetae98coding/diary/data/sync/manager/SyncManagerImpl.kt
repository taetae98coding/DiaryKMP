package io.github.taetae98coding.diary.data.sync.manager

import io.github.taetae98coding.diary.core.work.api.SyncWorkManager
import io.github.taetae98coding.diary.core.work.api.SyncWorkState
import io.github.taetae98coding.diary.data.sync.di.SyncManagerScope
import io.github.taetae98coding.diary.domain.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single
import kotlin.time.Duration
import kotlin.uuid.Uuid

@Single
internal class SyncManagerImpl(
    private val syncWorkManager: SyncWorkManager,
    @SyncManagerScope scope: CoroutineScope,
) : SyncManager {
    private val reportRequest =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    override val isProgressReported: StateFlow<Boolean> =
        merge(
            reportRequest.map { SyncProgressEvent.Report },
            syncWorkManager.state.map { state -> SyncProgressEvent.Work(state = state) },
        ).scan(SyncProgress()) { progress, event -> progress.next(event = event) }
            .map { progress -> progress.isProgressReported }
            .distinctUntilChanged()
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

    override fun requestSync(
        accountId: Uuid,
        reportsProgress: Boolean,
    ) {
        if (reportsProgress) {
            reportRequest.tryEmit(Unit)
        }

        syncWorkManager.sync(accountId = accountId)
    }

    override fun schedulePeriodicSync(
        accountId: Uuid,
        period: Duration,
    ) {
        syncWorkManager.schedulePeriodicSync(accountId = accountId, period = period)
    }

    override fun cancelPeriodicSync() {
        syncWorkManager.cancelPeriodicSync()
    }
}

private sealed interface SyncProgressEvent {
    data object Report : SyncProgressEvent

    data class Work(
        val state: SyncWorkState,
    ) : SyncProgressEvent
}

private data class SyncProgress(
    val isReportRequested: Boolean = false,
    val state: SyncWorkState = SyncWorkState.NONE,
) {
    val isProgressReported: Boolean = isReportRequested && state == SyncWorkState.RUNNING

    fun next(event: SyncProgressEvent): SyncProgress =
        when (event) {
            is SyncProgressEvent.Report -> copy(isReportRequested = true)

            is SyncProgressEvent.Work ->
                copy(
                    isReportRequested = isReportRequested && event.state != SyncWorkState.NONE,
                    state = event.state,
                )
        }
}
