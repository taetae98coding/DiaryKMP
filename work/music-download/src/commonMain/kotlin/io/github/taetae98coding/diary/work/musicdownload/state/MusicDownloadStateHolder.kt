package io.github.taetae98coding.diary.work.musicdownload.state

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
internal class MusicDownloadStateHolder {
    val stateMap: StateFlow<Map<MusicDownloadTarget, MusicDownloadState>>
        field = MutableStateFlow(emptyMap<MusicDownloadTarget, MusicDownloadState>())

    fun submitPending(targetList: List<MusicDownloadTarget>) {
        stateMap.update { current ->
            current + targetList.associateWith { target -> current[target] ?: MusicDownloadState.Pending }
        }
    }

    fun update(
        target: MusicDownloadTarget,
        state: MusicDownloadState,
    ) {
        stateMap.update { current -> current + (target to state) }
    }

    fun clearUnfinished() {
        stateMap.update { current ->
            current.filterValues { state -> state is MusicDownloadState.Done || state is MusicDownloadState.Failed }
        }
    }
}
