@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class PlaylistHomeViewModel(
    pageMusicUseCase: PageMusicUseCase,
    private val deleteMusicUseCase: DeleteMusicUseCase,
    private val restoreMusicUseCase: RestoreMusicUseCase,
) : ViewModel() {
    private val sort = MutableStateFlow(ListSort.TITLE)
    private val inProgressIds = mutableSetOf<Uuid>()

    val uiState: StateFlow<PlaylistHomeUiState> =
        sort
            .map { value -> PlaylistHomeUiState(sort = value) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = PlaylistHomeUiState(sort = sort.value),
            )

    val musicPagingData: Flow<PagingData<Music>> =
        sort
            .flatMapLatest { value -> pageMusicUseCase(parameter = value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val _effect = Channel<PlaylistHomeEffect>(Channel.BUFFERED)
    val effect: Flow<PlaylistHomeEffect> = _effect.receiveAsFlow()

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun delete(id: Uuid) {
        if (!inProgressIds.add(id)) return

        viewModelScope.launch {
            try {
                deleteMusicUseCase(parameter = id)
                    .onSuccess { _effect.send(PlaylistHomeEffect.Deleted(id = id)) }
            } finally {
                inProgressIds.remove(id)
            }
        }
    }

    fun restore(id: Uuid) {
        if (!inProgressIds.add(id)) return

        viewModelScope.launch {
            try {
                restoreMusicUseCase(parameter = id)
            } finally {
                inProgressIds.remove(id)
            }
        }
    }
}
