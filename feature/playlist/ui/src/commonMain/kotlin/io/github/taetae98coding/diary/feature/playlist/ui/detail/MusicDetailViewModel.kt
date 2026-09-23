package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkBlankException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FetchYoutubeVideoUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.UpdateMusicUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class MusicDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val updateMusicUseCase: UpdateMusicUseCase,
    private val deleteMusicUseCase: DeleteMusicUseCase,
    private val fetchYoutubeVideoUseCase: FetchYoutubeVideoUseCase,
    findMusicUseCase: FindMusicUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isLinkFetchInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<MusicDetailUiState> =
        combine(
            findMusicUseCase(parameter = id),
            isUpdateInProgress,
            isLinkFetchInProgress,
            isDeleteInProgress,
        ) { result, isUpdateInProgress, isLinkFetchInProgress, isDeleteInProgress ->
            result.getOrNull()?.let { music ->
                MusicDetailUiState.Content(
                    id = music.id,
                    detail = music.detail,
                    isUpdateInProgress = isUpdateInProgress,
                    isLinkFetchInProgress = isLinkFetchInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: MusicDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = MusicDetailUiState.Loading,
        )

    private val _effect = Channel<MusicDetailEffect>(Channel.BUFFERED)
    val effect: Flow<MusicDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: MusicDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updateMusicUseCase(parameter = UpdateMusicUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(MusicDetailEffect.UpdateSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is MusicLinkNotYoutubeException -> _effect.send(MusicDetailEffect.LinkNotYoutube)
                        }
                    }
            } finally {
                isUpdateInProgress.value = false
            }
        }
    }

    fun fetchLink(link: String) {
        if (isLinkFetchInProgress.value) return

        viewModelScope.launch {
            isLinkFetchInProgress.value = true
            try {
                fetchYoutubeVideoUseCase(parameter = link)
                    .onSuccess { video ->
                        _effect.send(
                            MusicDetailEffect.LinkFetched(
                                title = video.title,
                                artist = video.channelName,
                                thumbnail = video.thumbnail,
                            ),
                        )
                    }.onFailure { throwable -> sendFetchFailureEffect(throwable = throwable) }
            } finally {
                isLinkFetchInProgress.value = false
            }
        }
    }

    fun delete() {
        if (isDeleteInProgress.value) return

        viewModelScope.launch {
            isDeleteInProgress.value = true
            try {
                deleteMusicUseCase(parameter = id)
                    .onSuccess { _effect.send(MusicDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }

    private suspend fun sendFetchFailureEffect(throwable: Throwable) {
        when (throwable) {
            is MusicLinkBlankException -> _effect.send(MusicDetailEffect.LinkBlank)
            is MusicLinkNotYoutubeException -> _effect.send(MusicDetailEffect.LinkNotYoutube)
            else -> _effect.send(MusicDetailEffect.LinkFetchFailed)
        }
    }
}
