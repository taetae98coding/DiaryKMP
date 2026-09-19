package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.exception.MusicArtistBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicTitleBlankException
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
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
) : ViewModel() {
    val uiState: StateFlow<MusicAddUiState>
        field = MutableStateFlow(MusicAddUiState())

    private val _effect = Channel<MusicAddEffect>(Channel.BUFFERED)
    val effect: Flow<MusicAddEffect> = _effect.receiveAsFlow()

    fun add(detail: MusicDetail) {
        if (uiState.value.isInProgress) return

        viewModelScope.launch {
            uiState.update { value -> value.copy(isInProgress = true) }
            try {
                addMusicUseCase(parameter = detail)
                    .onSuccess { _effect.send(MusicAddEffect.AddSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is MusicTitleBlankException -> _effect.send(MusicAddEffect.TitleBlank)
                            is MusicArtistBlankException -> _effect.send(MusicAddEffect.ArtistBlank)
                        }
                    }
            } finally {
                uiState.update { value -> value.copy(isInProgress = false) }
            }
        }
    }
}
