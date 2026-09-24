package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadEvent
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadEventUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.GetMusicDownloadStateUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RequestMusicDownloadUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class PlaylistHomeDownloadViewModel(
    getMusicDownloadStateUseCase: GetMusicDownloadStateUseCase,
    getMusicDownloadEventUseCase: GetMusicDownloadEventUseCase,
    private val requestMusicDownloadUseCase: RequestMusicDownloadUseCase,
) : ViewModel() {
    val effect: Flow<PlaylistHomeDownloadEffect> =
        getMusicDownloadEventUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull()?.toEffect() }

    val uiState: StateFlow<PlaylistHomeDownloadUiState> =
        getMusicDownloadStateUseCase(parameter = Unit)
            .map { result -> PlaylistHomeDownloadUiState(stateMap = result.getOrNull().orEmpty()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = PlaylistHomeDownloadUiState(),
            )

    private fun MusicDownloadEvent.toEffect(): PlaylistHomeDownloadEffect =
        when (this) {
            MusicDownloadEvent.TOOL_NOT_INSTALLED -> PlaylistHomeDownloadEffect.ToolNotInstalled
            MusicDownloadEvent.TOOL_PREPARE_FAILED -> PlaylistHomeDownloadEffect.ToolPrepareFailed
        }

    fun download(sort: ListSort) {
        viewModelScope.launch {
            requestMusicDownloadUseCase(parameter = sort)
        }
    }
}
