package io.github.taetae98coding.diary.work.musicdownload.state

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
internal class MusicDownloadStateHolder {
    val stateMap: StateFlow<Map<Uuid, MusicDownloadState>>
        field = MutableStateFlow(emptyMap<Uuid, MusicDownloadState>())

    fun submitPending(idList: List<Uuid>) {
        stateMap.update { current ->
            current + idList.associateWith { id -> current[id] ?: MusicDownloadState.Pending }
        }
    }

    fun update(
        id: Uuid,
        state: MusicDownloadState,
    ) {
        stateMap.update { current -> current + (id to state) }
    }

    fun clearUnfinished() {
        stateMap.update { current ->
            current.filterValues { state -> state is MusicDownloadState.Done || state is MusicDownloadState.Failed }
        }
    }
}
