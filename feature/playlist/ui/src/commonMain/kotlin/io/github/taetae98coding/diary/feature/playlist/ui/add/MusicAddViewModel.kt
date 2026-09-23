package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicTitleBlankException
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FetchYoutubeVideoUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MusicAddViewModel(
    private val addMusicUseCase: AddMusicUseCase,
    private val fetchYoutubeVideoUseCase: FetchYoutubeVideoUseCase,
) : ViewModel() {
    val uiState: StateFlow<MusicAddUiState>
        field = MutableStateFlow(MusicAddUiState())

    private val _effect = Channel<MusicAddEffect>(Channel.BUFFERED)
    val effect: Flow<MusicAddEffect> = _effect.receiveAsFlow()

    private var fetchJob: Job? = null

    fun add(detail: MusicDetail) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addMusicUseCase(parameter = detail)
                    .onSuccess {
                        fetchJob?.cancel()
                        _effect.send(MusicAddEffect.AddSucceeded)
                    }.onFailure { throwable -> sendAddFailureEffect(throwable = throwable) }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }

    fun fetchLink(link: String) {
        if (uiState.value.isLinkFetchInProgress) return

        fetchJob =
            viewModelScope.launch {
                uiState.update { value -> value.copy(isLinkFetchInProgress = true) }
                try {
                    fetchYoutubeVideoUseCase(parameter = link)
                        .onSuccess { video ->
                            _effect.send(
                                MusicAddEffect.LinkFetched(
                                    title = video.title,
                                    artist = video.channelName,
                                ),
                            )
                        }.onFailure { throwable -> sendFetchFailureEffect(throwable = throwable) }
                } finally {
                    uiState.update { value -> value.copy(isLinkFetchInProgress = false) }
                }
            }
    }

    private suspend fun sendAddFailureEffect(throwable: Throwable) {
        when (throwable) {
            is MusicTitleBlankException -> _effect.send(MusicAddEffect.TitleBlank)
            is MusicLinkNotYoutubeException -> _effect.send(MusicAddEffect.LinkNotYoutube)
        }
    }

    private suspend fun sendFetchFailureEffect(throwable: Throwable) {
        when (throwable) {
            is MusicLinkBlankException -> _effect.send(MusicAddEffect.LinkBlank)
            is MusicLinkNotYoutubeException -> _effect.send(MusicAddEffect.LinkNotYoutube)
            else -> _effect.send(MusicAddEffect.LinkFetchFailed)
        }
    }
}
